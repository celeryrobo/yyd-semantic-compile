package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.ansj.util.MyStaticValue;

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
				String varName = word.substring(1);
				if (MyStaticValue.ENV.containsKey(varName)) {
					entTypes.add(varName);
				}
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Template) {
			Template tpl = (Template) obj;
			return template.equals(tpl.template);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return template.hashCode();
	}
}
