package com.ybnf.compiler.lucene.parsers;

import com.ybnf.compiler.lucene.TemplateBuilder;


public class Text extends Node<String> {
	public Text(String text) {
		this.data = text;
	}

	@Override
	public TemplateBuilder build() {
		TemplateBuilder builder = new TemplateBuilder();
		builder.add(new StringBuilder(data));
		return builder;
	}
}