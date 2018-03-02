package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Template {
	private String template;
	private List<String> keywords;
	private List<String> entTypes;

	public Template(String template) {
		this.template = template;
		this.keywords = new ArrayList<>();
		this.entTypes = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(template);
		while (tokenizer.hasMoreTokens()) {
			String word = tokenizer.nextToken();
			if (word.startsWith("$")) {
				entTypes.add(word.substring(1));
			} else {
				keywords.add(word);
			}
		}
	}

	public String getTemplate() {
		return template;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public List<String> getEntTypes() {
		return entTypes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{template=").append(template);
		sb.append(", entTypes=").append(entTypes);
		sb.append(", keywords=").append(keywords);
		sb.append("}");
		return sb.toString();
	}
}
