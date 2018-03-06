package com.ybnf.compiler.lucene.parsers;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.ybnf.compiler.lucene.TemplateBuilder;

public class Sent extends Node<List<Node<?>>> {
	public Sent(Stack<Node<?>> stack) {
		this.data = new LinkedList<>(stack);
	}

	@Override
	public TemplateBuilder build() {
		TemplateBuilder builder = new TemplateBuilder();
		for (Node<?> node : data) {
			if (builder.isEmpty()) {
				builder.add(node.build());
			} else {
				builder.append(node.build());
			}
		}
		return builder;
	}
}
