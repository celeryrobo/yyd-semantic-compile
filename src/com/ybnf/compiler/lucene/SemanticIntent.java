package com.ybnf.compiler.lucene;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.ansj.library.DicLibrary;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

public class SemanticIntent {
	private String name;
	private Set<String> entTypes;
	private List<Template> templates;
	private Forest forest;

	public SemanticIntent(String name) {
		this.name = name;
		this.templates = new LinkedList<>();
		this.entTypes = new HashSet<>();
		this.forest = DicLibrary.get();
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
		TemplateBuilder builder = build(buildTree(bnfTpl));
		for (Template template : builder.build()) {
			addTemplate(template);
		}
	}

	public List<Template> getTemplates() {
		return templates;
	}

	private List<Object> buildTree(String tpl) {
		Stack<Object> stack = new Stack<>();
		StringTokenizer tokenizer = new StringTokenizer(tpl, "[ ]", true);
		while (tokenizer.hasMoreTokens()) {
			Object token = tokenizer.nextToken();
			if ("]".equals(token)) {
				Stack<Object> kw = new Stack<>();
				while (true) {
					token = stack.pop();
					if ("[".equals(token)) {
						List<Object> objects = new LinkedList<>();
						while (!kw.isEmpty()) {
							objects.add(kw.pop());
						}
						stack.push(objects);
						break;
					}
					kw.push(token);
				}
			} else if (!" ".equals(token)) {
				stack.push(token);
			}
		}
		return new LinkedList<>(stack);
	}

	@SuppressWarnings("unchecked")
	private TemplateBuilder build(List<Object> tree) {
		TemplateBuilder result = new TemplateBuilder();
		result.add(new StringBuilder());
		for (Object obj : tree) {
			if (obj instanceof List) {
				TemplateBuilder rs = build((List<Object>) obj);
				TemplateBuilder builder = new TemplateBuilder();
				for (StringBuilder sb : rs.getBuilders()) {
					TemplateBuilder r = new TemplateBuilder(result);
					for (StringBuilder b : r.getBuilders()) {
						b.append(sb);
					}
					builder.add(r);
				}
				result.add(builder);
			} else {
				for (StringBuilder sb : result.getBuilders()) {
					String token = (String) obj;
					if (token.startsWith("$")) {
						sb.append(" ").append(token).append(" ");
					} else {
						sb.append(obj);
					}
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name).append("[\n");
		for (Template template : templates) {
			builder.append("    ").append(template).append("\n");
		}
		builder.append("[");
		return builder.toString();
	}
}
