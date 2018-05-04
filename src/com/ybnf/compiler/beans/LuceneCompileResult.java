package com.ybnf.compiler.beans;

import com.ybnf.compiler.lucene.TemplateEntity;

public class LuceneCompileResult extends YbnfCompileResult {
	private TemplateEntity templateEntity;

	public LuceneCompileResult(YbnfCompileResult ybnfCompileResult, TemplateEntity templateEntity) {
		super(ybnfCompileResult.getText(), ybnfCompileResult.getVersion(), ybnfCompileResult.getCharset(),
				ybnfCompileResult.getService(), ybnfCompileResult.getObjects(), ybnfCompileResult.getSlots());
		this.templateEntity = templateEntity;
	}

	public TemplateEntity getTemplateEntity() {
		return templateEntity;
	}
}
