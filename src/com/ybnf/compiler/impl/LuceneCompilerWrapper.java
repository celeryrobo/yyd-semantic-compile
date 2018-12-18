package com.ybnf.compiler.impl;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.lucene.search.Query;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.compiler.lucene.IndexReaderService;
import com.ybnf.compiler.lucene.SemanticSentence;
import com.ybnf.compiler.lucene.SemanticService;
import com.ybnf.compiler.lucene.TemplateEntity;

public class LuceneCompilerWrapper implements ICompiler {
	private static final Logger LOG = Logger.getLogger(LuceneCompilerWrapper.class.getSimpleName());
	private SemanticService service;
	private String path;

	public LuceneCompilerWrapper(SemanticService service, String path) {
		this.service = service;
		this.path = path;
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		SemanticSentence sentence = service.buildSentence(text);
		Query query = sentence.buildQuery();
		LOG.info(Optional.ofNullable(query).map(q -> q.toString()).orElse("no query"));
		List<TemplateEntity> entities = null;
		try (IndexReaderService readerService = new IndexReaderService(path)) {
			entities = readerService.search(query);
		}
		return sentence.compile(entities);
	}
}
