package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.StringUtil;

import com.ybnf.compiler.beans.YbnfCompileResult;

public class SemanticSentence {
	private String intent = "";
	private String lang;
	private String service;
	private Set<String> types;
	private Set<String> entTypes;
	private List<String> keywords;
	private List<String> sentences;

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
	}

	private void initSentence(String lang, Forest... forests) {
		Result result = DicAnalysis.parse(lang, forests);
		System.out.println(result);
		for (org.ansj.domain.Term term : result) {
			String natureStr = term.getNatureStr();
			String name = term.getName();
			if ("kv".equals(natureStr)) {
				keywords.add(name);
				sentences.add(name);
			} else if (natureStr.startsWith("c:")) {
				types.add(natureStr.substring(2));
				sentences.add(name);
			}
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

	private Map<String, String> buildSentence(String text, String template) throws Exception {
		Set<String> names = new HashSet<>();
		StringBuilder regexBuilder = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		String entities = StringUtil.joiner(sentences, "|");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				String name = token.substring(1);
				regexBuilder.append("(?<").append(name).append(">").append(entities).append(")");
				names.add(name);
			} else {
				regexBuilder.append(token);
			}
		}
		System.out.println("Regex : " + regexBuilder);
		Pattern pattern = Pattern.compile(regexBuilder.toString());
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			Map<String, String> objects = new HashMap<>();
			for (String name : names) {
				objects.put(name, matcher.group(name));
			}
			return objects;
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
			booleanBuilder.add(new TermQuery(new Term(fieldName, type.toLowerCase())), Occur.SHOULD);
		}
		booleanBuilder.add(new TermQuery(new Term("service", service)), Occur.MUST);
		return booleanBuilder.build();
	}

	public YbnfCompileResult compile(String template) throws Exception {
		Map<String, String> objects = buildSentence(lang, template);
		Map<String, String> slots = new HashMap<>();
		if (!intent.isEmpty()) {
			slots.put("intent", intent);
		}
		return new YbnfCompileResult(lang, "0.1", "UTF8", service, objects, slots);
	}

	public YbnfCompileResult compile(TemplateEntity templateEntity) throws Exception {
		return intent(templateEntity.getIntent()).compile(templateEntity.getTemplate());
	}

	public YbnfCompileResult compile(List<TemplateEntity> templateEntities) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (TemplateEntity templateEntity : templateEntities) {
			try {
				return compile(templateEntity);
			} catch (Exception e) {
				sb.append(e).append("\n");
			}
		}
		throw new Exception(sb.toString());
	}
}
