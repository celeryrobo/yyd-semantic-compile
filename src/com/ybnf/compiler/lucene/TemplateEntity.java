package com.ybnf.compiler.lucene;

public class TemplateEntity {
	private String service;
	private String intent;
	private String template;
	private float score;

	public TemplateEntity(String service, String intent, String template) {
		this(service, intent, template, 0);
	}

	public TemplateEntity(String service, String intent, String template, float score) {
		this.service = service;
		this.intent = intent;
		this.template = template;
		this.score = score;
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

	public float getScore() {
		return score;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("service=").append(service).append(", ");
		builder.append("intent=").append(intent).append(", ");
		builder.append("template=").append(template).append(", ");
		builder.append("score=").append(score).append("}");
		return builder.toString();
	}
}
