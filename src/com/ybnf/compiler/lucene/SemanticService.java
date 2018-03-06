package com.ybnf.compiler.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticService {
	private String name;
	private Map<String, SemanticIntent> intents;

	public SemanticService(String name) {
		this.name = name;
		this.intents = new HashMap<>();
	}

	public String getName() {
		return name;
	}
	
	public Map<String, SemanticIntent> getIntents() {
		return intents;
	}
	
	public SemanticIntent buildIntent(String intentName) {
		SemanticIntent intent = null;
		if(intents.containsKey(intentName)) {
			intent = intents.get(intentName);
		}else {
			intent = new SemanticIntent(name, intentName);
			intents.put(intentName, intent);
		}
		return intent;
	}

	public SemanticSentence buildSentence(String lang) {
		Set<String> types = new HashSet<>();
		for (SemanticIntent intent : intents.values()) {
			types.addAll(intent.getEntTypes());
		}
		return new SemanticSentence(name, lang, types);
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
