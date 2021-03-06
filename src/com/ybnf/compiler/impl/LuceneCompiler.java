package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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

public class LuceneCompiler implements ICompiler {
	private static final Logger LOG = Logger.getLogger(LuceneCompiler.class.getSimpleName());
	public static final Map<String, SemanticService> SERVICES = new HashMap<>();
	private static final ThreadLocal<Integer> APP_ID = new ThreadLocal<>();
	private SemanticService semanticService = null;
	private static String LUCENE_PATH;

	public static void init(String path, Map<String, Map<String, List<String>>> sceneIntentTemplates) throws Exception {
		LUCENE_PATH = path;
		if (sceneIntentTemplates == null) {
			return;
		}
		List<String> dics = new LinkedList<>();
		dics.add("dic");
		sceneIntentTemplates.forEach((k, v) -> {
			StringBuilder sb = new StringBuilder("SRV-").append(k);
			dics.add(sb.toString());
		});
		try (IndexWriterService writerService = new IndexWriterService(LUCENE_PATH, dics)) {
			writerService.deleteAll();
			for (Entry<String, Map<String, List<String>>> sceneIntentTemplate : sceneIntentTemplates.entrySet()) {
				String sceneName = sceneIntentTemplate.getKey();
				SemanticService service = new SemanticService(sceneName);
				for (Entry<String, List<String>> intentTemplate : sceneIntentTemplate.getValue().entrySet()) {
					String intentName = intentTemplate.getKey();
					SemanticIntent intent = service.buildIntent(intentName);
					intentTemplate.getValue().forEach(e -> intent.addTemplate(e, writerService));
				}
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

	public void setAppId(Integer appId) {
		APP_ID.set(appId);
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		SemanticSentence sentence = semanticService.buildSentence(text);
		Query query = sentence.buildQuery();
		LOG.info(Optional.ofNullable(query).map(q -> q.toString()).orElse("no query"));
		List<TemplateEntity> entities = null;
		try (IndexReaderService readerService = new IndexReaderService(LUCENE_PATH)) {
			entities = readerService.search(query);
		}
		return sentence.compile(entities);
	}
}
