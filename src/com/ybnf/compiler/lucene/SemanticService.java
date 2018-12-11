package com.ybnf.compiler.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticService {
	private String name;
	private Integer appId;
	private Map<String, SemanticIntent> intents;

	public SemanticService(String name, Integer appId) {
		this.name = name;
		this.appId = appId;
		this.intents = new HashMap<>();
	}

	public String getName() {
		return name;
	}

	public Integer getAppId() {
		return appId;
	}

	public Map<String, SemanticIntent> getIntents() {
		return intents;
	}

	public SemanticIntent buildIntent(String intentName) {
		SemanticIntent intent = intents.get(intentName);
		if (intent == null) {
			intent = new SemanticIntent(this, intentName);
			intents.put(intentName, intent);
		}
		return intent;
	}

	public SemanticSentence buildSentence(String lang) {
		Set<String> types = new HashSet<>();
		Set<String> varTypes = new HashSet<>();
		for (SemanticIntent intent : intents.values()) {
			types.addAll(intent.getEntTypes());
			varTypes.addAll(intent.getVarTypes());
		}
		return new SemanticSentence(this, lang, types, varTypes);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name).append("(").append(appId).append(")").append("{\n");
		for (SemanticIntent intent : intents.values()) {
			builder.append(intent).append("\n");
		}
		builder.append("}");
		return builder.toString();
	}
}
