package com.ybnf.compiler.lucene;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.StringUtil;

import com.ybnf.compiler.beans.LuceneCompileResult;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.expr.Expr;
import com.ybnf.expr.ExprService;

public class SemanticSentence {
	private static final Logger LOG = Logger.getLogger(SemanticSentence.class.getSimpleName());
	private String intent = "";
	private String lang;
	private String service;
	private Set<String> types;
	private Set<String> entTypes;
	private Set<String> varTypes;
	private List<String> keywords;
	private List<String> sentences;
	private ExprService dsl;

	public SemanticSentence(String service, String lang, Set<String> entTypes, Set<String> varTypes) {
		this.lang = lang;
		this.service = service;
		this.entTypes = entTypes;
		this.varTypes = varTypes;
		this.types = new HashSet<>();
		this.keywords = new LinkedList<>();
		this.sentences = new LinkedList<>();
		this.dsl = new ExprService();
		Forest[] forests = new Forest[entTypes.size() + 2];
		forests[0] = DicLibrary.get(); // 默认词库
		forests[1] = DicLibrary.get("SRV" + service); // 当前场景内的关键词词库
		Forest[] dics = DicLibrary.gets(this.entTypes);
		for (int i = 2; i < forests.length; i++) {
			forests[i] = dics[i - 2];
		}
		initSentence(lang, forests);
		initDslSentence(types, sentences);
	}

	private void initSentence(String lang, Forest... forests) {
		Result result = IndexAnalysis.parse(lang, forests);
		new YydDicNatureRecognition(varTypes, forests).recognition(result);
		LOG.info(result.toString());
		for (org.ansj.domain.Term term : result) {
			String natureStr = term.getNatureStr();
			String name = term.getName();
			sentences.add(name);
			if ("kv".equals(natureStr)) {
				keywords.add(name);
			} else if (natureStr.startsWith("c:")) {
				types.add(natureStr.substring(2));
			}
		}
		int[] sentArr = new int[lang.length()];
		keywords.forEach(e -> {
			int pos = lang.indexOf(e);
			int len = e.length();
			for (int i = pos; i < len + pos; i++) {
				sentArr[i] = 1;
			}
		});
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < sentArr.length; i++) {
			if (1 == sentArr[i]) {
				builder.append((char) lang.charAt(i));
			} else {
				builder.append("|");
			}
		}
		keywords = new LinkedList<>();
		for (String name : builder.toString().split("\\|")) {
			if (!"".equals(name)) {
				keywords.add(name);
			}
		}
		LOG.info("Keywords: " + keywords + ", Sentences: " + sentences);
	}

	private void initDslSentence(Collection<String> vars, Collection<String> sentences) {
		if (sentences == null || sentences.isEmpty()) {
			return;
		}
		try {
			Expr parser = ParserUtils.generate(StringUtil.joiner(sentences, "|"), null);
			for (String type : vars) {
				dsl.include(type, parser);
			}
		} catch (Exception e) {
		}
	}

	public SemanticSentence intent(String intent) {
		this.intent = intent;
		return this;
	}

	public Query buildQuery(Integer companyId) {
		if (keywords.isEmpty() && varTypes.isEmpty()) {
			return null;
		}
		BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
		for (String keyword : keywords) {
			booleanBuilder.add(new TermQuery(new Term("template", keyword)), Occur.MUST);
		}
		for (String type : varTypes) {
			Query query = new TermQuery(new Term("template", type.toLowerCase()));
			if (types.contains(type)) {
				booleanBuilder.add(new BoostQuery(query, 4.0F), Occur.SHOULD);
			} else {
				booleanBuilder.add(query, Occur.SHOULD);
			}
		}
		booleanBuilder.add(new TermQuery(new Term("service", service)), Occur.MUST);
		if ("QA".equals(service)) {
			String companyIdStr = Objects.toString(companyId, "0");
			booleanBuilder.add(new TermQuery(new Term("companyId", companyIdStr)), Occur.MUST);
		}
		return booleanBuilder.build();
	}

	public YbnfCompileResult compile(String template) throws Exception {
		Map<String, String> slots = new HashMap<>();
		if (intent != null) {
			slots.put("intent", intent);
		}
		Map<String, String> objects = dsl.compile(template, lang);
		return new YbnfCompileResult(lang, "1.0", "UTF-8", service, objects, slots);
	}

	public YbnfCompileResult compile(TemplateEntity templateEntity) throws Exception {
		return new LuceneCompileResult(intent(templateEntity.getIntent()).compile(templateEntity.getTemplate()),
				templateEntity);
	}

	public YbnfCompileResult compile(List<TemplateEntity> templateEntities) throws Exception {
		StringBuilder sb = new StringBuilder("\n");
		for (TemplateEntity templateEntity : templateEntities) {
			try {
				return compile(templateEntity);
			} catch (Exception e) {
				sb.append(templateEntity).append(" : ").append(e).append("\n");
			}
		}
		throw new Exception(sb.toString());
	}
}
