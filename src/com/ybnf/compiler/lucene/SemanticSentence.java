package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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

	private String getSentence(String template) {
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		int index = 0, kvIndex = 0;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				kvIndex++;
				continue;
			}
			index = sentences.indexOf(token);
			break;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = index - kvIndex; i < sentences.size(); i++) {
			builder.append(sentences.get(i));
		}
		return builder.toString();
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
			booleanBuilder.add(new TermQuery(new Term(fieldName, type.toLowerCase())), Occur.SHOULD);
		}
		booleanBuilder.add(new TermQuery(new Term("service", service)), Occur.MUST);
		return booleanBuilder.build();
	}

	public YbnfCompileResult compile(String template) throws Exception {
		Set<String> _types = new HashSet<>();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				_types.add(token.substring(1));
			}
		}
		StringBuilder sb = new StringBuilder("#YBNF 1.0 utf8;");
		sb.append("service ").append(service).append(";root $main;").append("$main");
		if (!intent.isEmpty()) {
			sb.append("{intent%").append(intent).append("}");
		}
		sb.append(" = ").append(template).append(";");
		String entities = StringUtil.joiner(sentences, "|");
		for (String type : _types) {
			sb.append("$").append(type).append("{").append(type).append("} = ").append(entities).append(";");
		}
		ICompiler compiler = new JCompiler(sb.toString());
		String sent = getSentence(template);
		System.out.println("Sentence : " + sent);
		return compiler.compile(sent);
	}

	public YbnfCompileResult compile(TemplateEntity templateEntity) throws Exception {
		return intent(templateEntity.getIntent()).compile(templateEntity.getTemplate());
	}
}
