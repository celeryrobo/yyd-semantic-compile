package com.ybnf.compiler.lucene;

public class QAEntity {
	private Integer id;
	private Integer appId;
	private String question;

	public QAEntity() {}
	
	public QAEntity(Integer id, Integer appId, String question) {
		this.id = id;
		this.appId = appId;
		this.question = question;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	@Override
	public String toString() {
		return "QAEntity [id=" + id + ", appId=" + appId + ", question=" + question + "]";
	}
}
