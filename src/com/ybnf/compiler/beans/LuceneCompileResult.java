package com.ybnf.compiler.beans;

import com.ybnf.compiler.lucene.TemplateEntity;

public class LuceneCompileResult extends YbnfCompileResult {
	private TemplateEntity templateEntity;
	private Integer appId;
	public LuceneCompileResult(YbnfCompileResult ybnfCompileResult, TemplateEntity templateEntity) {
		super(ybnfCompileResult.getText(), ybnfCompileResult.getVersion(), ybnfCompileResult.getCharset(),
				ybnfCompileResult.getService(), ybnfCompileResult.getObjects(), ybnfCompileResult.getSlots());
		this.templateEntity = templateEntity;
	}
	
	public LuceneCompileResult(YbnfCompileResult ybnfCompileResult, Integer appId) {
		super(ybnfCompileResult.getText(), ybnfCompileResult.getVersion(), ybnfCompileResult.getCharset(),
				ybnfCompileResult.getService(), ybnfCompileResult.getObjects(), ybnfCompileResult.getSlots());
		this.appId=appId;
	}

	public TemplateEntity getTemplateEntity() {
		return templateEntity;
	}

	public Integer getAppId() {
		return appId;
	}
}
