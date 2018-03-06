package com.ybnf.compiler.lucene.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ybnf.compiler.lucene.TemplateBuilder;

public class Varname extends Node<Node<?>> {
	private static final Pattern PATTERN = Pattern.compile("\\w+");

	public Varname(Node<?> node) {
		StringBuilder sb = new StringBuilder();
		sb.append(node.data);
		Matcher matcher = PATTERN.matcher(sb);
		if (matcher.matches()) {
			this.data = node;
		} else {
			throw new RuntimeException("varname is error !" + data);
		}
	}

	@Override
	public TemplateBuilder build() {
		TemplateBuilder builder = new TemplateBuilder();
		builder.add(new StringBuilder(" $"));
		builder.append(data.build());
		builder.append(new StringBuilder(" "));
		return builder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("$").append(data);
		return builder.toString();
	}
}
