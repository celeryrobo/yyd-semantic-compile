package com.ybnf.compiler.lucene;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.StringUtil;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.compiler.impl.JCompiler;

public class SemanticSentence {
	private String intent = "";
	private String service;
	private Set<String> types;
	private Set<String> entTypes;
	private List<String> keywords;
	private List<String> sentences;

	public SemanticSentence(String service, String lang, Set<String> entTypes) {
		this.service = service;
		this.entTypes = entTypes;
		this.types = new HashSet<>();
		this.keywords = new LinkedList<>();
		this.sentences = new LinkedList<>();
		System.out.println(entTypes);
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

	public List<String> getSentences() {
		List<String> sents = new LinkedList<>();
		int size = sentences.size();
		for (int i = 0; i < size; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = i; j < size; j++) {
				sb.append(sentences.get(j));
			}
			sents.add(sb.toString());
		}
		System.out.println("--->>>:" + sents);
		return sents;
	}

	public Query buildQuery(String fieldName) {
		BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
		if (!keywords.isEmpty()) {
			PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();
			phraseBuilder.setSlop(10);
			int idx = 0;
			for (String keyword : keywords) {
				phraseBuilder.add(new Term(fieldName, keyword), idx++);
			}
			booleanBuilder.add(phraseBuilder.build(), Occur.MUST);
		}
		for (String type : types) {
			booleanBuilder.add(new TermQuery(new Term(fieldName, type.toLowerCase())), Occur.MUST);
		}
		for (String entType : entTypes) {
			if (!types.contains(entType)) {
				booleanBuilder.add(new TermQuery(new Term(fieldName, entType.toLowerCase())), Occur.MUST_NOT);
			}
		}
		booleanBuilder.add(new TermQuery(new Term("service", service)), Occur.MUST);
		return booleanBuilder.build();
	}

	public YbnfCompileResult compile(String template) throws Exception {
		StringBuilder sb = new StringBuilder("#YBNF 1.0 utf8;");
		sb.append("service ").append(service).append(";root $main;");
		sb.append("$main");
		if (!intent.isEmpty()) {
			sb.append("{intent%").append(intent).append("}");
		}
		sb.append(" = ").append(template).append(";");
		String entities = StringUtil.joiner(sentences, "|");
		for (String type : types) {
			sb.append("$").append(type).append("{").append(type).append("} = ").append(entities).append(";");
		}
		ICompiler compiler = new JCompiler(sb.toString());
		YbnfCompileResult result = null;
		for (String sent : getSentences()) {
			try {
				System.out.println(sent);
				result = compiler.compile(sent);
				if (result != null) {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
