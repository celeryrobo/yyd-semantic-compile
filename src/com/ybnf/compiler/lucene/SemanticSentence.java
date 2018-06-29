package com.ybnf.compiler.lucene;

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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.StringUtil;

import com.ybnf.compiler.beans.LuceneCompileResult;
import com.ybnf.compiler.beans.YbnfCompileResult;
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
	private Map<String, Set<String>> sentences;
	private ExprService dsl;

	public SemanticSentence(String service, String lang, Set<String> entTypes, Set<String> varTypes) {
		this.lang = lang;
		this.service = service;
		this.entTypes = entTypes;
		this.varTypes = varTypes;
		this.types = new HashSet<>();
		this.keywords = new LinkedList<>();
		this.sentences = new HashMap<>();
		this.dsl = new ExprService();
		int entTypeSize = entTypes.size();
		Forest[] forests = new Forest[entTypeSize + 2];
		Forest[] dics = DicLibrary.gets(this.entTypes);
		int index = 0;
		for (; index < entTypeSize; index++) {
			forests[index] = dics[index];
		}
		forests[index] = DicLibrary.get(); // 默认词库
		forests[index + 1] = DicLibrary.get("SRV" + service); // 当前场景内的关键词词库
		initSentence(lang, forests);
		initDslSentence(sentences);
	}

	private void initSentence(String lang, Forest... forests) {
		Result result = IndexAnalysis.parse(lang, forests);
		new YydDicNatureRecognition(varTypes, forests).recognition(result);
		LOG.info(result.toString());
		int[] sentArr = new int[lang.length()];
		int wordIndex = 0;
		boolean isKeyword = false;
		for (org.ansj.domain.Term term : result) {
			String natureStr = term.getNatureStr();
			String name = term.getName();
			if ("kv".equals(natureStr)) {
				isKeyword = true;
			} else if (natureStr.startsWith("c:")) {
				isKeyword = false;
				String type = natureStr.substring(2);
				types.add(type);
				if (!sentences.containsKey(type)) {
					sentences.put(type, new HashSet<>());
				}
				sentences.get(type).add(name);
			} else {
				continue;
			}
			int pos = term.getOffe();
			int len = name.length();
			if (pos < wordIndex) {
				continue;
			}
			wordIndex = pos + len;
			if (isKeyword) {
				for (int i = pos; i < len + pos; i++) {
					sentArr[i] = 1;
				}
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < sentArr.length; i++) {
			if (1 == sentArr[i]) {
				builder.append(lang.charAt(i));
			} else if (builder.length() != 0) {
				keywords.add(builder.toString());
				builder = new StringBuilder();
			}
		}
		if (builder.length() != 0) {
			keywords.add(builder.toString());
		}
		LOG.info("Keywords: " + keywords + ", Sentences: " + sentences);
	}

	private void initDslSentence(Map<String, Set<String>> sentences) {
		if (sentences == null || sentences.isEmpty()) {
			return;
		}
		try {
			for (String key : sentences.keySet()) {
				dsl.include(key, ParserUtils.generate(StringUtil.joiner(sentences.get(key), "|"), null));
			}
		} catch (Exception e) {
		}
	}

	public SemanticSentence intent(String intent) {
		this.intent = intent;
		return this;
	}

	public Query buildQuery(Integer companyId) {
		if (keywords.isEmpty() && types.isEmpty()) {
			return null;
		}
		BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
		for (String keyword : keywords) {
			booleanBuilder.add(new TermQuery(new Term("template", keyword)), Occur.MUST);
		}
		for (String type : types) {
			Query query = new TermQuery(new Term("template", type.toLowerCase()));
			booleanBuilder.add(query, Occur.SHOULD);
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
		if (templateEntities.isEmpty()) {
			sb.append("Template Empty!!!");
		} else {
			for (TemplateEntity templateEntity : templateEntities) {
				try {
					return compile(templateEntity);
				} catch (Exception e) {
					sb.append(templateEntity).append(" : ").append(e).append("\n");
				}
			}
		}
		throw new Exception(sb.toString());
	}
}
