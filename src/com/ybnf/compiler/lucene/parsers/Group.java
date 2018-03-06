package com.ybnf.compiler.lucene.parsers;

import java.util.LinkedList;
import java.util.List;

import com.ybnf.compiler.lucene.TemplateBuilder;

public class Group extends Node<List<Node<?>>> {
	public Group() {
		this.data = new LinkedList<>();
	}

	public void add(Node<?> node) {
		data.add(node);
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("(");
		int size = data.size();
		for (int i = 0; i < size; i++) {
			builder.append(data.get(i));
			if (i < size - 1) {
				builder.append(", ");
			}
		}
		builder.append(")");
		return builder.toString();
	}
}
