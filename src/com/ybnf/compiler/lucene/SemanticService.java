package com.ybnf.compiler.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticService {
	private String name;
	private Set<String> entTypes;
	private Map<String, SemanticIntent> intents;

	public SemanticService(String name) {
		this.name = name;
		this.intents = new HashMap<>();
		this.entTypes = new HashSet<>();
	}

	public String getName() {
		return name;
	}
	
	public Map<String, SemanticIntent> getIntents() {
		return intents;
	}

	public void addIntent(SemanticIntent intent) {
		String _name = intent.getName();
		entTypes.addAll(intent.getEntTypes());
		if (intents.containsKey(_name)) {
			SemanticIntent _intent = intents.get(_name);
			for (Template template : intent.getTemplates()) {
				_intent.addTemplate(template);
			}
		} else {
			intents.put(_name, intent);
		}
	}

	public SemanticSentence buildSentence(String lang) {
		return new SemanticSentence(name, lang, entTypes);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name).append("{\n");
		for (SemanticIntent intent : intents.values()) {
			builder.append(intent).append("\n");
		}
		builder.append("}");
		return builder.toString();
	}
}
