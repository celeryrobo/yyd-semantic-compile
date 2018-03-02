package com.ybnf.compiler.lucene;

import java.util.LinkedList;
import java.util.List;

public class TemplateBuilder {
	private List<StringBuilder> builders;

	public TemplateBuilder() {
		builders = new LinkedList<>();
	}

	public TemplateBuilder(TemplateBuilder builder) {
		this();
		for (StringBuilder sb : builder.builders) {
			builders.add(new StringBuilder(sb.toString()));
		}
	}
	
	public List<StringBuilder> getBuilders() {
		return builders;
	}

	public boolean isEmpty() {
		return builders.isEmpty();
	}
	
	public void add(StringBuilder sb) {
		builders.add(sb);
	}
	
	public void add(TemplateBuilder builder) {
		builders.addAll(builder.builders);
	}
	
	public List<Template> build(){
		List<Template> templates = new LinkedList<>();
		for (StringBuilder sb : builders) {
			templates.add(new Template(sb.toString().trim()));
		}
		return templates;
	}
	
	@Override
	public String toString() {
		return builders.toString();
	}
}
