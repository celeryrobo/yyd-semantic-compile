package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.nlpcn.commons.lang.tire.domain.Forest;

import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.expr.Expr;
import com.ybnf.expr.ExprService;
import com.ybnf.expr.impl.Or;
import com.ybnf.expr.impl.Word;

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
		this.sentences = new ArrayList<>();
		Forest[] forests = new Forest[entTypes.size() + 2];
		forests[0] = DicLibrary.get(); // 默认词库
		forests[1] = DicLibrary.get("SRV" + service); // 当前场景内的关键词词库
		Forest[] dics = DicLibrary.gets(this.entTypes);
		for (int i = 2; i < forests.length; i++) {
			forests[i] = dics[i - 2];
		}
		initSentence(lang, forests);
		initDslService();
	}

	private void initSentence(String lang, Forest... forests) {
		Result result = IndexAnalysis.parse(lang, forests);
		new YydDicNatureRecognition(varTypes, forests).recognition(result);
		LOG.info(result.toString());
		List<org.ansj.domain.Term> terms = filterTerms(lang, result);
		LOG.info(terms.toString());
		for (org.ansj.domain.Term term : terms) {
			String natureStr = term.getNatureStr();
			String name = term.getName();
			sentences.add(name);
			if ("kv".equals(natureStr)) {
				keywords.add(name);
			}
		}
	}

	private List<org.ansj.domain.Term> filterTerms(String lang, Result result) {
		List<org.ansj.domain.Term> terms = new LinkedList<>();
		int totalSize = 0, curSize = 0, curIndex = 0;
		for (org.ansj.domain.Term term : result) {
			String realName = term.getRealName();
			String natureStr = term.getNatureStr();
			curIndex = lang.indexOf(realName, totalSize - curSize - 1);
			if (curIndex >= totalSize) {
				curSize = realName.length();
				totalSize += curSize;
				terms.add(term);
			}
			if (natureStr.startsWith("c:")) {
				types.add(natureStr.substring(2));
			}
		}
		return terms;
	}

	private void initDslService() {
		dsl = new ExprService();
		if (sentences.isEmpty()) {
			return;
		}
		Expr parser = null;
		int size = sentences.size();
		switch (size) {
		case 1: {
			parser = new Word(sentences.get(0));
			break;
		}
		case 2: {
			parser = new Or(new Word(sentences.get(0)), new Word(sentences.get(1)));
			break;
		}
		default: {
			Expr[] arr = new Expr[size - 2];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = new Word(sentences.get(i + 2));
			}
			parser = new Or(new Word(sentences.get(0)), new Word(sentences.get(1)), arr);
			break;
		}
		}
		for (String type : types) {
			dsl.include(type, parser);
		}
	}

	public SemanticSentence intent(String intent) {
		this.intent = intent;
		return this;
	}

	public Set<String> getTypes() {
		return types;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public Query buildQuery(String fieldName) {
		if (keywords.isEmpty() && varTypes.isEmpty()) {
			return null;
		}
		BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
		if (!keywords.isEmpty()) {
			PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();
			phraseBuilder.setSlop(5);
			int idx = 0;
			for (String keyword : keywords) {
				phraseBuilder.add(new Term(fieldName, keyword), idx++);
			}
			booleanBuilder.add(phraseBuilder.build(), Occur.MUST);
		}
		for (String type : varTypes) {
			booleanBuilder.add(new TermQuery(new Term(fieldName, type.toLowerCase())), Occur.SHOULD);
		}
		booleanBuilder.add(new TermQuery(new Term("service", service)), Occur.MUST);
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
		return intent(templateEntity.getIntent()).compile(templateEntity.getTemplate());
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
