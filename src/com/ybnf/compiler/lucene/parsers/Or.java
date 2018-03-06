package com.ybnf.compiler.lucene.parsers;

import java.util.LinkedList;
import java.util.List;

import com.ybnf.compiler.lucene.TemplateBuilder;

public class Or extends Node<List<Node<?>>> {
	public Or(Node<?> first, Node<?> second) {
		this.data = new LinkedList<>();
		this.data.add(first);
		this.data.add(second);
	}

	@Override
	public TemplateBuilder build() {
		TemplateBuilder builder = new TemplateBuilder();
		for (Node<?> node : data) {
			builder.add(node.build());
		}
		return builder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(data.get(0)).append(" | ").append(data.get(1));
		return builder.toString();
	}
}
