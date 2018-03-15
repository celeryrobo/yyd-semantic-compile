package com.ybnf.compiler.lucene;

import java.util.HashSet;
import java.util.Set;

import org.ansj.library.DicLibrary;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

public class SemanticIntent {
	private String name;
	private Set<String> entTypes;
	private Set<Template> templates;
	private Forest forest;

	SemanticIntent(String name) {
		this.name = name;
		this.entTypes = new HashSet<>();
		this.forest = DicLibrary.get();
		resetTemplates();
	}

	public String getName() {
		return name;
	}
	
	public Set<String> getEntTypes() {
		return entTypes;
	}

	public void addTemplate(Template template) {
		templates.add(template);
		entTypes.addAll(template.getEntTypes());
		for (String keyword : template.getKeywords()) {
			Library.insertWord(forest, new Value(keyword, "kv", "1"));
		}
	}

	public void addTemplate(String bnfTpl) {
		try {
			TemplateBuilder builder = ParserUtils.parse(bnfTpl);
			for (Template template : builder.build()) {
				addTemplate(template);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<Template> getTemplates() {
		return templates;
	}
	
	public void resetTemplates() {
		templates = new HashSet<>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name).append("[\n");
		for (Template template : templates) {
			builder.append("    ").append(template).append("\n");
		}
		builder.append("]");
		return builder.toString();
	}
}
