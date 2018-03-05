package com.ybnf;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.search.Query;

import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.compiler.lucene.IndexReaderService;
import com.ybnf.compiler.lucene.IndexWriterService;
import com.ybnf.compiler.lucene.SemanticIntent;
import com.ybnf.compiler.lucene.SemanticSentence;
import com.ybnf.compiler.lucene.SemanticService;
import com.ybnf.compiler.lucene.TemplateEntity;

public class Main {
	public static void main(String[] args) throws Exception {
		List<String> tpls = new LinkedList<>();
		tpls.add("[[[我] (想 | 要)] 听] $singer 的 (歌 | $song)");
		tpls.add("[[我] (想 | 要)] 听 [[一] 首] (歌 | $song)");

		SemanticService service = new SemanticService("music");
		initIntent(service, "play", tpls);

		test(service, "听刘德华的冰雨");
		long start = System.currentTimeMillis();
		test(service, "听刘德华的冰雨吧");
		test(service, "我想听刘德华的冰雨呀");
		test(service, "我好想听刘德华的冰雨呀");
		test(service, "我想听歌");
		test(service, "我想听冰雨");
		System.out.println(System.currentTimeMillis() - start);
	}

	private static void initIntent(SemanticService service, String name, List<String> tpls) throws Exception {
		SemanticIntent intent = new SemanticIntent(name);
		for (String tpl : tpls) {
			intent.addTemplate(tpl);
		}
		service.addIntent(intent);
		System.out.println(service);
		try (IndexWriterService writerService = new IndexWriterService()) {
			writerService.initSemanticService(service);
		}
	}

	private static void test(SemanticService service, String lang) throws Exception {
		SemanticSentence sentence = service.buildSentence(lang);
		Query query = sentence.buildQuery("template");
		System.out.println(query);
		TemplateEntity entity = null;
		try (IndexReaderService readerService = new IndexReaderService()) {
			entity = readerService.search(query);
		}
		if (entity == null) {
			System.out.println("fail ...");
		} else {
			YbnfCompileResult result = sentence.intent(entity.getIntent()).compile(entity.getTemplate());
			System.out.println(result);
		}
	}
}
