package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
		try {
			while (tokenizer.hasMoreTokens()) {
				stack.push(parser(stack, tokenizer));
			}
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder(e.getMessage());
			sb.append(" ").append(lang);
			throw new Exception(sb.toString());
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

	private static final Pattern COMMON_PATTERN = Pattern.compile("\\(\\?\\<\\w+\\>\\.\\+\\)");

	private static String tplParse(Stack<Expr> stacks, StringTokenizer tokenizer, Map<String, Expr> includes)
			throws Exception {
		String token = tokenizer.nextToken();
		switch (token) {
		case "|":
			if (tokenizer.hasMoreTokens()) {
				tplParse(stacks, tokenizer, includes);
				Expr second = stacks.pop();
				Expr first = stacks.pop();
				stacks.push(new com.ybnf.expr.impl.Or(first, second));
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
			Expr expr = stacks.pop();
			String regex = expr.expr();
			boolean isCommon = COMMON_PATTERN.matcher(regex).matches();
			if (!isCommon) {
				expr = new ZeroOrMany(expr);
			}
			stacks.push(expr);
			break;
		}
		case "+": {
			Expr expr = stacks.pop();
			String regex = expr.expr();
			boolean isCommon = COMMON_PATTERN.matcher(regex).matches();
			if (!isCommon) {
				expr = new OneOrMany(expr);
			}
			stacks.push(expr);
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
				temp = source == target ? 0 : 1;
				vecs[row][col] = Math.min(temp + vecs[row - 1][col - 1],
						Math.min(vecs[row][col - 1] + 1, vecs[row - 1][col] + 1));
			}
		}
		int distance = vecs[sourceLen][targetLen];
		return 1 - (float) distance / Math.max(sourceLen, targetLen);
	}

	public static float distanceScoreWithTemplate(String sourceTemplate, String targetTemplate) {
		final String delim = " *";
		List<String> sources = new ArrayList<>();
		StringTokenizer sourceTokenizer = new StringTokenizer(sourceTemplate, delim);
		while (sourceTokenizer.hasMoreTokens()) {
			String token = sourceTokenizer.nextToken();
			if (!"".equals(token)) {
				sources.add(token);
			}
		}
		List<String> targets = new ArrayList<>();
		StringTokenizer targetTokenizer = new StringTokenizer(targetTemplate, delim);
		while (targetTokenizer.hasMoreTokens()) {
			String token = targetTokenizer.nextToken();
			if (!"".equals(token)) {
				targets.add(token);
			}
		}
		int sourceSize = sources.size();
		int targetSize = targets.size();
		int rowSize = sourceSize + 1;
		int colSize = targetSize + 1;
		int[][] vecs = new int[rowSize][colSize];
		for (int col = 0; col < colSize; col++) {
			vecs[0][col] = col;
		}
		for (int row = 0; row < rowSize; row++) {
			vecs[row][0] = row;
		}
		String source, target;
		int temp;
		for (int row = 1; row < rowSize; row++) {
			source = sources.get(row - 1);
			for (int col = 1; col < colSize; col++) {
				target = targets.get(col - 1);
				temp = Objects.equals(source, target) ? 0 : 1;
				vecs[row][col] = Math.min(temp + vecs[row - 1][col - 1],
						Math.min(vecs[row][col - 1] + 1, vecs[row - 1][col] + 1));
			}
		}
		int distance = vecs[sourceSize][targetSize];
		return 1 - (float) distance / Math.max(sourceSize, targetSize);
	}

	public static boolean isKeyword(String keyword) {
		return "kv".equals(keyword);
	}

	public static boolean isCategory(String category) {
		if (Objects.equals(category, "")) {
			return false;
		}
		return category.startsWith("c:");
	}

	public static void recognition(String lang, Result result) {
		Iterator<Term> terms = result.iterator();
		int[] arr = new int[lang.length()];
		int pos = 0, len = 0;
		while (terms.hasNext()) {
			Term term = terms.next();
			String natureStr = term.getNatureStr();
			int length = term.getName().length();
			int position = term.getOffe();
			boolean isRemoved = false;
			if (isKeyword(natureStr)) {
				for (int i = position; i < position + length; i++) {
					if (0 == arr[i]) {
						arr[i] = 1;
					} else {
						isRemoved = true;
						break;
					}
				}
			} else if (isCategory(natureStr)) {
				if (position < pos + len) {
					if (position == pos && length == len) {
						isRemoved = false;
					} else {
						isRemoved = true;
					}
				} else {
					for (int i = position; i < position + length; i++) {
						if (0 == arr[i]) {// || 2 == arr[i]
							arr[i] = 2;
						} else {
							isRemoved = true;
							break;
						}
					}
				}
			} else {
				isRemoved = true;
			}
			if (isRemoved) {
				terms.remove();
			} else if (position > pos) {
				pos = position;
				len = length;
			}
		}
	}
}
