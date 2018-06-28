package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SemanticService {
	private String name;
	private Map<String, SemanticIntent> intents;
	private List<String> entitiesPrioritiy;

	public SemanticService(String name, List<String> entitiesPrioritiy) {
		this.name = name;
		this.intents = new HashMap<>();
		this.entitiesPrioritiy = entitiesPrioritiy;
	}

	public String getName() {
		return name;
	}

	public Map<String, SemanticIntent> getIntents() {
		return intents;
	}

	public SemanticIntent buildIntent(String intentName) {
		SemanticIntent intent = intents.get(intentName);
		if (intent == null) {
			intent = new SemanticIntent(name, intentName);
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
		List<String> entTypes = new ArrayList<>();
		Optional<List<String>> entitiesPrioritiyOptional = Optional.ofNullable(entitiesPrioritiy);
		entTypes.addAll(entitiesPrioritiyOptional
				.map(ep -> types.stream().filter(e -> !ep.contains(e)).collect(Collectors.toList()))
				.orElseGet(() -> types.stream().collect(Collectors.toList())));
		entitiesPrioritiyOptional.ifPresent(
				ep -> entTypes.addAll(ep.stream().filter(e -> types.contains(e)).collect(Collectors.toList())));
		return new SemanticSentence(name, lang, entTypes, varTypes);
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
