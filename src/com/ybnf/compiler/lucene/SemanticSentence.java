package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.StringUtil;

import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.dsl.DslService;
import com.ybnf.dsl.parser.Parser;
import com.ybnf.dsl.parser.impl.ORR;
import com.ybnf.dsl.parser.impl.WORD;

public class SemanticSentence {
	private static final Logger LOG = Logger.getLogger(SemanticSentence.class.getSimpleName());
	private String intent = "";
	private String lang;
	private String service;
	private Set<String> types;
	private Set<String> entTypes;
	private List<String> keywords;
	private List<String> sentences;
	private DslService dsl;

	public SemanticSentence(String service, String lang, Set<String> entTypes) {
		this.lang = lang;
		this.service = service;
		this.entTypes = entTypes;
		this.types = new HashSet<>();
		this.keywords = new LinkedList<>();
		this.sentences = new ArrayList<>();
		Forest[] forests = new Forest[entTypes.size() + 1];
		forests[0] = DicLibrary.get();
		Forest[] dics = DicLibrary.gets(this.entTypes);
		for (int i = 1; i < forests.length; i++) {
			forests[i] = dics[i - 1];
		}
		initSentence(lang, forests);
		initDslService();
	}

	private void initSentence(String lang, Forest... forests) {
		Result result = IndexAnalysis.parse(lang, forests);
		new UserDicNatureRecognition(forests).recognition(result);
		LOG.info(result.toString());
		List<org.ansj.domain.Term> terms = filterTerms(lang, result);
		LOG.info(terms.toString());
		for (org.ansj.domain.Term term : terms) {
			String natureStr = term.getNatureStr();
			String name = term.getName();
			sentences.add(name);
			if ("kv".equals(natureStr)) {
				keywords.add(name);
			} else if (natureStr.startsWith("c:")) {
				types.add(natureStr.substring(2));
			}
		}
	}

	private List<org.ansj.domain.Term> filterTerms(String lang, Result result) {
		List<org.ansj.domain.Term> terms = new LinkedList<>();
		int totalSize = 0, curSize = 0, curIndex = 0;
		for (org.ansj.domain.Term term : result) {
			String realName = term.getRealName();
			curIndex = lang.indexOf(realName, totalSize - curSize - 1);
			if (curIndex >= totalSize) {
				curSize = realName.length();
				totalSize += curSize;
				terms.add(term);
			}
		}
		return terms;
	}

	private void initDslService() {
		dsl = new DslService();
		if (sentences.isEmpty()) {
			return;
		}
		Parser parser = null;
		int size = sentences.size();
		switch (size) {
		case 1: {
			parser = new WORD(sentences.get(0));
			break;
		}
		case 2: {
			parser = new ORR(new WORD(sentences.get(0)), new WORD(sentences.get(1)));
			break;
		}
		default: {
			Parser[] arr = new Parser[size - 2];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = new WORD(sentences.get(i + 2));
			}
			parser = new ORR(new WORD(sentences.get(0)), new WORD(sentences.get(1)), arr);
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

	private String buildSentence(String text, String template) throws Exception {
		StringBuilder regexBuilder = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		String sentence = StringUtil.joiner(sentences, "|");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				regexBuilder.append("(").append(sentence).append(")");
			} else {
				regexBuilder.append(token);
			}
		}
		LOG.info("Regex : " + regexBuilder);
		Matcher matcher = Pattern.compile(regexBuilder.toString()).matcher(text);
		if (matcher.find()) {
			return matcher.group();
		}
		throw new Exception("Semantic Match Failture !");
	}

	public Query buildQuery(String fieldName) {
		BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
		if (!keywords.isEmpty()) {
			PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();
			phraseBuilder.setSlop(4);
			int idx = 0;
			for (String keyword : keywords) {
				phraseBuilder.add(new Term(fieldName, keyword), idx++);
			}
			booleanBuilder.add(phraseBuilder.build(), Occur.MUST);
		}
		for (String type : types) {
			if (entTypes.contains(type)) {
				booleanBuilder.add(new TermQuery(new Term(fieldName, type.toLowerCase())), Occur.SHOULD);
			}
		}
		if (keywords.isEmpty() && types.isEmpty()) {
			return null;
		}
		booleanBuilder.add(new TermQuery(new Term("service", service)), Occur.MUST);
		return booleanBuilder.build();
	}

	public YbnfCompileResult compile(String template) throws Exception {
		String sentence = buildSentence(lang, template);
		Map<String, String> slots = new HashMap<>();
		if (intent != null) {
			slots.put("intent", intent);
		}
		Map<String, String> objects = dsl.compile(template, sentence);
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
