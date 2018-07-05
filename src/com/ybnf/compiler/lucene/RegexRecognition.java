package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Nature;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.Recognition;

public class RegexRecognition implements Recognition {
	private static final long serialVersionUID = 1L;
	public static final Nature IGNORE_NATURE = new Nature("ignore");
	private Pattern pattern;
	private Nature nature;

	public RegexRecognition(String regex, String nature) {
		this.pattern = Pattern.compile(regex);
		this.nature = new Nature(nature);
	}

	@Override
	public void recognition(Result result) {
		List<Term> regexTerms = new ArrayList<>();
		List<Term> terms = result.getTerms();
		for (Term term : terms) {
			// 分词后为关键词词性则不再进行分词
			String natureStr = term.getNatureStr();
			if (ParserUtils.isKeyword(natureStr) || ParserUtils.isCategory(natureStr)) {
				regexTerms.add(term);
				continue;
			}
			String realName = term.getRealName();
			Matcher matcher = pattern.matcher(realName);
			if (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				Term tm = null;
				if (start != 0) {
					tm = new Term(realName.substring(0, start), term.getOffe(), term.item());
					tm.setNature(IGNORE_NATURE);
					regexTerms.add(tm);
				}
				tm = new Term(matcher.group(), term.getOffe() + start, term.item());
				tm.setNature(nature);
				regexTerms.add(tm);
				if (end != realName.length()) {
					tm = new Term(realName.substring(end), term.getOffe() + end, term.item());
					tm.setNature(IGNORE_NATURE);
					regexTerms.add(tm);
				}
			} else {
				regexTerms.add(term);
			}
		}
		terms.clear();
		terms.addAll(regexTerms);
	}

}
