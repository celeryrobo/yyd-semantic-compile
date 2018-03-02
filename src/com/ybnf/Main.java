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
		tpls.add("[[我] 想] 听 $singer 的歌");
		tpls.add("[[我] 想] 听 $singer 的 $song");

		SemanticService service = new SemanticService("music");
		initIntent(service, "play", tpls);
		test(service, "我想听刘德华的歌");
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
		try (IndexReaderService readerService = new IndexReaderService()) {
			SemanticSentence sentence = service.buildSentence(lang);
			Query query = sentence.buildQuery("template");
			System.out.println(query);
			TemplateEntity entity = readerService.search(query);
			if (entity != null) {
				YbnfCompileResult result = sentence.intent(entity.getIntent()).compile(entity.getTemplate());
				System.out.println(result);
			} else {
				System.out.println("fail ...");
			}
		}
	}
}
