package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import com.ybnf.compiler.impl.JCompiler;

class Sentence {
	private String lang;
	private List<String> keywords;

	public Sentence(String lang, List<String> keywords) {
		this.lang = lang;
		this.keywords = keywords;
	}

	public Sentence(String lang) {
		this(lang, new LinkedList<>());
	}

	public String getLang() {
		return lang;
	}

	public List<String> getKeywords() {
		return keywords;
	}
}

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

	private Sentence buildSentence(String text, String template) throws Exception {
		StringBuilder regexBuilder = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				regexBuilder.append("(.+)");
			} else {
				regexBuilder.append(token);
			}
		}
		System.out.println("Regex : " + regexBuilder);
		Pattern pattern = Pattern.compile(regexBuilder.toString());
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			String sent = matcher.group();
			Sentence sentence = new Sentence(sent);
			for (int i = 0; i < matcher.groupCount(); i++) {
				sentence.getKeywords().add(matcher.group(i + 1));
			}
			return sentence;
		}
		throw new Exception("Semantic Match Failture !");
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
		// 获取模板中的变量
		Set<String> _types = new HashSet<>();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				_types.add(token.substring(1));
			}
		}
		// 根据模板与变量构建YBNF语法
		StringBuilder sb = new StringBuilder("#YBNF 1.0 utf8;");
		sb.append("service ").append(service).append(";root $main;").append("$main");
		if (!intent.isEmpty()) {
			sb.append("{intent%").append(intent).append("}");
		}
		sb.append(" = ").append(template).append(";");
		Sentence sent = buildSentence(lang, template);
		Set<String> sents = new HashSet<>();
		sents.addAll(sentences);
		sents.addAll(sent.getKeywords());
		String entities = StringUtil.joiner(sents, "|");
		for (String type : _types) {
			sb.append("$").append(type).append("{").append(type).append("} = ").append(entities).append(";");
		}
		System.out.println("Sentence : " + sent.getLang());
		// 使用构建好了的YBNF语法编译当前语料
		return new JCompiler(sb.toString()).compile(sent.getLang());
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
