package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private static Map<String, SemanticService> SERVICES = new HashMap<>();
	private SemanticService semanticService = null;

	private static void init(Map<String, Map<String, List<String>>> sceneIntentTemplates) throws Exception {
		try (IndexWriterService writerService = new IndexWriterService()) {
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

	public LuceneCompiler(Map<String, Map<String, List<String>>> sceneIntentTemplates) throws Exception {
		if (sceneIntentTemplates != null) {
			synchronized (SERVICES) {
				init(sceneIntentTemplates);
			}
		}
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		SemanticSentence sentence = semanticService.buildSentence(text);
		Query query = sentence.buildQuery("template");
		System.out.println(query);
		List<TemplateEntity> entities = null;
		try (IndexReaderService readerService = new IndexReaderService()) {
			entities = readerService.search(query);
		}
		System.out.println("TemplateEntity : ");
		for (TemplateEntity entity : entities) {
			System.out.println(entity);
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
