package com.ybnf.compiler.lucene;

import java.util.Stack;
import java.util.StringTokenizer;

import com.ybnf.compiler.lucene.parsers.Choices;
import com.ybnf.compiler.lucene.parsers.Group;
import com.ybnf.compiler.lucene.parsers.Node;
import com.ybnf.compiler.lucene.parsers.Or;
import com.ybnf.compiler.lucene.parsers.Sent;
import com.ybnf.compiler.lucene.parsers.Text;
import com.ybnf.compiler.lucene.parsers.Varname;

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
}
