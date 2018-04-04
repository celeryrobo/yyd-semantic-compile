package com.ybnf.compiler.lucene;

public class TemplateEntity {
	private String service;
	private String intent;
	private String template;

	public TemplateEntity(String service, String intent, String template) {
		this.service = service;
		this.intent = intent;
		this.template = template;
	}

	public String getService() {
		return service;
	}

	public String getIntent() {
		return intent;
	}

	public String getTemplate() {
		return template;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("service=").append(service).append(", ");
		builder.append("intent=").append(intent).append(", ");
		builder.append("template=").append(template).append("}");
		return builder.toString();
	}
}
