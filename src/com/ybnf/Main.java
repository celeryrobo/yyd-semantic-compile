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
		List<String> tplMusic = new LinkedList<>();
		tplMusic.add("[[[我] (想 | 要)] 听] $singer 的 (歌 | $song)");
		tplMusic.add("[[我] (想 | 要)] 听 [[一] 首] (歌 | $song)");
		
		List<String> tplPoetry = new LinkedList<>();
		tplPoetry.add("$poetry [的] (下 | 上) [一] 句");
		tplPoetry.add("背 [诵] [一] 首 ($poetryTitle | ($author 的诗))");

		SemanticService music = new SemanticService("music");
		SemanticService poetry = new SemanticService("poetry");
		
		initIntent(music, "play", tplMusic);
		initIntent(poetry, "poem", tplPoetry);
		
		test(music, "听刘德华的冰雨");
		long start = System.currentTimeMillis();
		test(music, "听刘德华的冰雨吧");
		test(music, "我想听刘德华的冰雨呀");
		test(music, "我好想听刘德华的冰雨呀");
		test(music, "我想听歌");
		test(music, "我想听冰雨");
		
		test(poetry, "床前明月光的下一句是什么");
		test(poetry, "床前明月光的上一句呢");
		test(poetry, "背首李白的诗");
		test(poetry, "背诵一首静夜诗");
		System.out.println(System.currentTimeMillis() - start);
	}

	private static void initIntent(SemanticService service, String name, List<String> tpls) throws Exception {
		SemanticIntent intent = service.buildIntent(name);
		for (String tpl : tpls) {
			intent.addTemplate(tpl);
		}
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
			System.out.println(entity);
		}
		if (entity == null) {
			System.out.println("fail ...");
		} else {
			YbnfCompileResult result = sentence.intent(entity.getIntent()).compile(entity.getTemplate());
			System.out.println(result);
		}
	}
}
