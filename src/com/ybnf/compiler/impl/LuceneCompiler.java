package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.lucene.search.Query;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.compiler.lucene.IndexReaderService;
import com.ybnf.compiler.lucene.IndexWriterService;
import com.ybnf.compiler.lucene.SemanticIntent;
import com.ybnf.compiler.lucene.SemanticSentence;
import com.ybnf.compiler.lucene.SemanticService;
import com.ybnf.compiler.lucene.TemplateEntity;
import com.ybnf.semantic.SemanticCallable;

public class LuceneCompiler implements ICompiler {
	private static final Logger LOG = Logger.getLogger(LuceneCompiler.class.getSimpleName());
	private Map<String, SemanticService> services = null;
	private SemanticService semanticService = null;

	public LuceneCompiler(Map<String, Map<String, List<String>>> sceneIntentTemplates) throws Exception {
		if (sceneIntentTemplates == null) {
			return;
		}
		try (IndexWriterService writerService = new IndexWriterService()) {
			services = new HashMap<>();
			for (Entry<String, Map<String, List<String>>> sceneIntentTemplate : sceneIntentTemplates.entrySet()) {
				String sceneName = sceneIntentTemplate.getKey();
				SemanticService service = new SemanticService(sceneName);
				for (Entry<String, List<String>> intentTemplate : sceneIntentTemplate.getValue().entrySet()) {
					String intentName = intentTemplate.getKey();
					SemanticIntent intent = service.buildIntent(intentName);
					for (String template : intentTemplate.getValue()) {
						intent.addTemplate(template);
					}
				}
				writerService.initSemanticService(service);
				services.put(sceneName, service);
			}
		}
	}

	public LuceneCompiler(LuceneCompiler compiler, String service) throws Exception {
		if (compiler == null || compiler.services == null) {
			throw new Exception("参数compiler为空或compiler.services为空！");
		}
		semanticService = compiler.services.get(service);
		if (semanticService == null) {
			throw new Exception("场景:" + service + "，不存在！");
		}
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		SemanticSentence sentence = semanticService.buildSentence(text);
		Query query = sentence.buildQuery("template");
		LOG.info(query.toString());
		List<TemplateEntity> entities = null;
		try (IndexReaderService readerService = new IndexReaderService()) {
			entities = readerService.search(query);
		}
		LOG.info("TemplateEntity : ");
		for (TemplateEntity entity : entities) {
			LOG.info(entity.toString());
		}
		YbnfCompileResult result = null;
		if (entities != null) {
			result = sentence.compile(entities);
		}
		return result;
	}

	@Override
	public void setSemanticCallable(SemanticCallable semanticCallable) {
	}

}
