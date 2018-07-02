package com.ybnf.compiler.lucene;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.ansj.domain.Result;
import org.ansj.domain.Term;

import com.ybnf.compiler.lucene.parsers.Choices;
import com.ybnf.compiler.lucene.parsers.Group;
import com.ybnf.compiler.lucene.parsers.Node;
import com.ybnf.compiler.lucene.parsers.Or;
import com.ybnf.compiler.lucene.parsers.Sent;
import com.ybnf.compiler.lucene.parsers.Text;
import com.ybnf.compiler.lucene.parsers.Varname;
import com.ybnf.expr.Expr;
import com.ybnf.expr.impl.NamedGroup;
import com.ybnf.expr.impl.OneOrMany;
import com.ybnf.expr.impl.Selectable;
import com.ybnf.expr.impl.Word;
import com.ybnf.expr.impl.ZeroOrMany;

public class ParserUtils {
	private static final Text CHOICES_LEFT = new Text("[");
	private static final Text CHOICES_RIGHT = new Text("]");
	private static final Text GROUP_LEFT = new Text("(");
	private static final Text GROUP_RIGHT = new Text(")");

	public static TemplateBuilder parse(String lang) throws Exception {
		String grammar = lang.replaceAll("\\s+", " ").trim();
		StringTokenizer tokenizer = new StringTokenizer(grammar, "$[]()| ", true);
		Stack<Node<?>> stack = new Stack<>();
		while (tokenizer.hasMoreTokens()) {
			stack.push(parser(stack, tokenizer));
		}
		return new Sent(stack).build();
	}

	private static Node<?> parser(Stack<Node<?>> stack, StringTokenizer tokenizer) throws Exception {
		final Stack<Node<?>> kvStack = new Stack<>();
		String token = tokenizer.nextToken();
		switch (token) {
		case "[":
			stack.push(CHOICES_LEFT);
			while (tokenizer.hasMoreTokens()) {
				Node<?> node = parser(stack, tokenizer);
				if (CHOICES_RIGHT.equals(node)) {
					while (true) {
						node = stack.pop();
						if (CHOICES_LEFT.equals(node)) {
							Choices choices = new Choices();
							while (!kvStack.isEmpty()) {
								choices.add(kvStack.pop());
							}
							return choices;
						}
						kvStack.push(node);
					}
				}
				stack.push(node);
			}
			throw new Exception("choices parser error !");
		case "(":
			stack.push(GROUP_LEFT);
			while (tokenizer.hasMoreTokens()) {
				Node<?> node = parser(stack, tokenizer);
				if (GROUP_RIGHT.equals(node)) {
					while (true) {
						node = stack.pop();
						if (GROUP_LEFT.equals(node)) {
							Group group = new Group();
							while (!kvStack.isEmpty()) {
								group.add(kvStack.pop());
							}
							return group;
						}
						kvStack.push(node);
					}
				}
				stack.push(node);
			}
			throw new Exception("group parser error !");
		case "|":
			if (tokenizer.hasMoreTokens()) {
				Node<?> second = parser(stack, tokenizer);
				Node<?> first = stack.pop();
				return new Or(first, second);
			}
			throw new Exception("or parser error !");
		case "$":
			if (tokenizer.hasMoreTokens()) {
				Node<?> node = parser(stack, tokenizer);
				if (node instanceof Text) {
					return new Varname(node);
				}
			}
			throw new Exception("varname parser error !");
		case " ":
			if (tokenizer.hasMoreTokens()) {
				return parser(stack, tokenizer);
			}
			throw new Exception("space parser error !");
		default:
			return new Text(token);
		}
	}

	public static Expr generate(String template, Map<String, Expr> includes) throws Exception {
		Stack<Expr> stacks = new Stack<>();
		StringTokenizer tokenizer = new StringTokenizer(template, "()[]|$*+ ", true);
		while (tokenizer.hasMoreTokens()) {
			tplParse(stacks, tokenizer, includes);
		}
		return tplBuilder(stacks);
	}

	private static Expr tplBuilder(List<Expr> stack) {
		int size = stack.size();
		switch (size) {
		case 0:
			return null;
		case 1:
			return stack.get(0);
		case 2:
			return new com.ybnf.expr.impl.Group(stack.get(0), stack.get(1));
		default: {
			Expr[] exprs = new Expr[size - 1];
			for (int i = 1; i < size; i++) {
				exprs[i - 1] = stack.get(i);
			}
			return new com.ybnf.expr.impl.Group(stack.get(0), exprs);
		}
		}
	}

	private static String tplParse(Stack<Expr> stacks, StringTokenizer tokenizer, Map<String, Expr> includes)
			throws Exception {
		String token = tokenizer.nextToken();
		switch (token) {
		case "|":
			if (tokenizer.hasMoreTokens()) {
				tplParse(stacks, tokenizer, includes);
				Expr second = stacks.pop();
				Expr first = stacks.pop();
				stacks.push(new com.ybnf.expr.impl.Group(new com.ybnf.expr.impl.Or(first, second)));
				break;
			}
			throw new Exception("or parser error");
		case "$":
			if (tokenizer.hasMoreTokens()) {
				String varName = tokenizer.nextToken();
				Expr expr = includes.get(varName);
				if (expr == null) {
					throw new Exception("var " + varName + " is not exsit");
				}
				stacks.push(new NamedGroup(varName, expr));
				break;
			}
			throw new Exception("var parser error");
		case "[": {
			Stack<Expr> tmpStacks = new Stack<>();
			while (tokenizer.hasMoreTokens()) {
				String tmpToken = tplParse(tmpStacks, tokenizer, includes);
				if ("]".equals(tmpToken)) {
					stacks.push(new Selectable(tplBuilder(tmpStacks)));
					return token;
				}
			}
			throw new Exception("selectable parser error");
		}
		case "(": {
			Stack<Expr> tmpStacks = new Stack<>();
			while (tokenizer.hasMoreTokens()) {
				String tmpToken = tplParse(tmpStacks, tokenizer, includes);
				if (")".equals(tmpToken)) {
					Expr expr = tplBuilder(tmpStacks);
					if (!(expr instanceof com.ybnf.expr.impl.Group)) {
						expr = new com.ybnf.expr.impl.Group(expr);
					}
					stacks.push(expr);
					return token;
				}
			}
			throw new Exception("group parser error");
		}
		case "*": {
			stacks.push(new ZeroOrMany(stacks.pop()));
			break;
		}
		case "+": {
			stacks.push(new OneOrMany(stacks.pop()));
			break;
		}
		case " ":
		case "]":
		case ")":
			break;
		default:
			stacks.push(new Word(token));
			break;
		}
		return token;
	}

	public static float distanceScore(String sourceStr, String targetStr) {
		int sourceLen = sourceStr.length();
		int targetLen = targetStr.length();
		int rowSize = sourceLen + 1;
		int colSize = targetLen + 1;
		int[][] vecs = new int[rowSize][colSize];
		for (int col = 0; col < colSize; col++) {
			vecs[0][col] = col;
		}
		for (int row = 0; row < rowSize; row++) {
			vecs[row][0] = row;
		}
		char source, target;
		int temp;
		for (int row = 1; row < rowSize; row++) {
			source = sourceStr.charAt(row - 1);
			for (int col = 1; col < colSize; col++) {
				target = targetStr.charAt(col - 1);
				if (source == target) {
					temp = 0;
				} else {
					temp = 1;
				}
				vecs[row][col] = Math.min(temp + vecs[row - 1][col - 1],
						Math.min(vecs[row][col - 1] + 1, vecs[row - 1][col] + 1));
			}
		}
		int distance = vecs[sourceLen][targetLen];
		return 1 - (float) distance / Math.max(sourceLen, targetLen);
	}

	public static void recognition(String lang, Result result) {
		Iterator<Term> terms = result.iterator();
		int pos = 0;
		while (terms.hasNext()) {
			Term term = terms.next();
			String natureStr = term.getNatureStr();
			if (!"kv".equals(natureStr) && !natureStr.startsWith("c:")) {
				terms.remove();
				continue;
			}
			int length = term.getName().length();
			int position = term.getOffe();
			if (pos <= position) {
				pos = position + length;
			} else {
				terms.remove();
				continue;
			}
		}
	}
}
