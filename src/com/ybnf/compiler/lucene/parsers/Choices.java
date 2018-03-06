package com.ybnf.compiler.lucene.parsers;

import java.util.LinkedList;
import java.util.List;

import com.ybnf.compiler.lucene.TemplateBuilder;

public class Choices extends Node<List<Node<?>>> {
	public Choices() {
		this.data = new LinkedList<>();
	}

	public void add(Node<?> node) {
		data.add(node);
	}

	@Override
	public TemplateBuilder build() {
		TemplateBuilder builder = null;
		for (Node<?> node : data) {
			if (builder == null) {
				builder = node.build();
			} else {
				builder.append(node.build());
			}
		}
		builder.add(new StringBuilder());
		return builder;
	}
}
