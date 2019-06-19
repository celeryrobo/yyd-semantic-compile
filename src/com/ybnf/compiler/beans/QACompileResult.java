package com.ybnf.compiler.beans;

import com.ybnf.compiler.lucene.QAEntity;

public class QACompileResult extends YbnfCompileResult {
	private QAEntity entity;
	private Integer appId;

	public QACompileResult(YbnfCompileResult ybnfCompileResult, QAEntity entity) {
		super(ybnfCompileResult.getText(), ybnfCompileResult.getVersion(), ybnfCompileResult.getCharset(),
				ybnfCompileResult.getService(), ybnfCompileResult.getObjects(), ybnfCompileResult.getSlots());
		this.setEntity(entity);
	}

	public QACompileResult(YbnfCompileResult ybnfCompileResult, Integer appId) {
		super(ybnfCompileResult.getText(), ybnfCompileResult.getVersion(), ybnfCompileResult.getCharset(),
				ybnfCompileResult.getService(), ybnfCompileResult.getObjects(), ybnfCompileResult.getSlots());
		this.setAppId(appId);
	}

	public QAEntity getEntity() {
		return entity;
	}

	public void setEntity(QAEntity entity) {
		this.entity = entity;
	}

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}
}
