package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.LinkedList;
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
	public static final Map<String, SemanticService> SERVICES = new HashMap<>();
	private static final ThreadLocal<Integer> COMPANY_ID = new ThreadLocal<>();
	private SemanticService semanticService = null;

	public static void init(Map<String, Map<String, List<String>>> sceneIntentTemplates) throws Exception {
		if (sceneIntentTemplates == null) {
			return;
		}
		List<String> dics = new LinkedList<>();
		dics.add("dic");
		for (String service : sceneIntentTemplates.keySet()) {
			dics.add("SRV" + service);
		}
		try (IndexWriterService writerService = new IndexWriterService(dics)) {
			writerService.deleteAll();
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
				SERVICES.put(sceneName, service);
			}
		}
	}

	public LuceneCompiler(String service) throws Exception {
		semanticService = SERVICES.get(service);
		if (semanticService == null) {
			throw new Exception("场景:" + service + "，不存在！");
		}
	}
	
	public void setCompanyId(Integer companyId) {
		COMPANY_ID.set(companyId);
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		SemanticSentence sentence = semanticService.buildSentence(text);
		Query query = sentence.buildQuery(COMPANY_ID.get());
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
