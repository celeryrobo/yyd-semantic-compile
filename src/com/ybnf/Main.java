package com.ybnf;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.compiler.impl.JCompiler;
import com.ybnf.compiler.impl.MITIECompiler;
import com.ybnf.compiler.lucene.IndexReaderService;
import com.ybnf.compiler.lucene.IndexWriterService;
import com.ybnf.compiler.lucene.SemanticIntent;
import com.ybnf.compiler.lucene.SemanticSentence;
import com.ybnf.compiler.lucene.SemanticService;
import com.ybnf.compiler.lucene.TemplateEntity;

public class Main {
	public static void main(String[] args) throws Exception {
		/*
		 * String path = "C:\\Users\\hongxinzhao\\Desktop\\owl\\"; String langModel =
		 * path + "total_word_feature_extractor.dat"; String categoryModel = path +
		 * "category_model.dat"; String nerModel = path + "ner_model.dat"; ICompiler m =
		 * new MITIECompiler(categoryModel, nerModel, langModel);
		 * System.out.println(m.compile("我想听刘德华的冰雨。"));
		 * System.out.println(m.compile("我想听我们不一样。"));
		 * System.out.println(m.compile("我要听火。"));
		 * System.out.println(m.compile("我想听断桥残雪。"));
		 * System.out.println(m.compile("想听莫文蔚的歌。"));
		 * System.out.println(m.compile("陆贞，什么时候的人？"));
		 * System.out.println(m.compile("李白是什么人？"));
		 * System.out.println(m.compile("苏轼是什么时候的人物？"));
		 * System.out.println(m.compile("我想听明天就要嫁给你了。"));
		 * System.out.println(m.compile("你好，我要听欢乐颂。"));
		 * 
		 * StringBuilder sb = new StringBuilder("#YBNF 1.0 utf8;");
		 * sb.append("service music;").append("root $main;").
		 * append("$main = 我想听 $singer 的歌;"); sb.append("$singer{singer} = 刘德华 | 张学友;");
		 * ICompiler c = new JCompiler(sb.toString());
		 * System.out.println(c.compile("我想听刘德华的歌"));
		 */

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
			TopDocs hits = readerService.search(query, 10);
			System.out.println(hits.totalHits);
			if (hits.totalHits > 0) {
				TemplateEntity entity = readerService.getTemplateEntity(hits.scoreDocs[0]);
				YbnfCompileResult result = sentence.intent(entity.getIntent()).compile(entity.getTemplate());
				System.out.println(result);
			}
		}
	}
}
