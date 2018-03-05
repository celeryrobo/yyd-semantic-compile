package com.ybnf.compiler.lucene;

import java.util.Stack;
import java.util.StringTokenizer;

import com.ybnf.compiler.lucene.tools.Choices;
import com.ybnf.compiler.lucene.tools.Group;
import com.ybnf.compiler.lucene.tools.Node;
import com.ybnf.compiler.lucene.tools.Or;
import com.ybnf.compiler.lucene.tools.Sent;
import com.ybnf.compiler.lucene.tools.Text;
import com.ybnf.compiler.lucene.tools.Varname;

public class ParserUtils {
	private static final Text CHOICE_LEFT = new Text("[");
	private static final Text GROUP_LEFT = new Text("(");
	private static final Stack<Node<?>> KW_STACK = new Stack<>();

	public static TemplateBuilder parse(String lang) {
		String grammar = lang.replaceAll("\\s+", " ");
		StringTokenizer tokenizer = new StringTokenizer(grammar, "$[]()| ", true);
		Stack<Node<?>> stack = new Stack<>();
		while (tokenizer.hasMoreTokens()) {
			parser(stack, tokenizer);
		}
		Sent sent = new Sent(stack);
		return sent.build();
	}

	private static String parser(Stack<Node<?>> stack, StringTokenizer tokenizer) {
		String token = tokenizer.nextToken();
		switch (token) {
		case "]":
			while (true) {
				Node<?> node = stack.pop();
				if (CHOICE_LEFT.equals(node)) {
					break;
				}
				KW_STACK.push(node);
			}
			Choices choices = new Choices();
			while (!KW_STACK.isEmpty()) {
				choices.add(KW_STACK.pop());
			}
			stack.push(choices);
			break;
		case ")":
			while (true) {
				Node<?> node = stack.pop();
				if (GROUP_LEFT.equals(node)) {
					break;
				}
				KW_STACK.push(node);
			}
			Group group = new Group();
			while (!KW_STACK.isEmpty()) {
				group.add(KW_STACK.pop());
			}
			stack.push(group);
			break;
		case "|":
			do {
				token = parser(stack, tokenizer);
			} while (" ".equals(token));
			Node<?> second = stack.pop();
			Node<?> first = stack.pop();
			stack.push(new Or(first, second));
			break;
		case "$":
			token = parser(stack, tokenizer);
			if (!" ".equals(token)) {
				Node<?> text = stack.pop();
				stack.push(new Varname(text));
			}
			break;
		case " ":
			break;
		default:
			stack.push(new Text(token));
			break;
		}
		return token;
	}
}
