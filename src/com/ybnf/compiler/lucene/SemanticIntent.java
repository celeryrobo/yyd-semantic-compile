package com.ybnf.compiler.lucene;

import java.util.HashSet;
import java.util.Set;

import org.ansj.library.DicLibrary;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

public class SemanticIntent {
	private String name;
	private String service;
	private Set<String> entTypes;
	private Set<String> varTypes;
	private Forest forest;

	SemanticIntent(String service, String name) {
		this.name = name;
		this.service = service;
		this.entTypes = new HashSet<>();
		this.varTypes = new HashSet<>();
		this.forest = DicLibrary.get("SRV" + service);
	}

	public String getName() {
		return name;
	}

	public Set<String> getEntTypes() {
		return entTypes;
	}

	public Set<String> getVarTypes() {
		return varTypes;
	}

	public void addTemplate(Template template, IndexWriterService indexWriterService) throws Exception {
		entTypes.addAll(template.getEntTypes());
		varTypes.addAll(template.getVarTypes());
		for (String keyword : template.getKeywords()) {
			Library.insertWord(forest, new Value(keyword, "kv", "1"));
		}
		indexWriterService.addTemplate(service, name, template);
	}

	public void addTemplate(String bnfTpl, IndexWriterService indexWriterService) {
		try {
			TemplateBuilder builder = ParserUtils.parse(bnfTpl);
			for (Template template : builder.build()) {
				addTemplate(template, indexWriterService);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SemanticIntent [").append(name).append("]");
		return builder.toString();
	}
}
