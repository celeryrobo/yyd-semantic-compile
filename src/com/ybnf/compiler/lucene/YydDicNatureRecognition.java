package com.ybnf.compiler.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.ansj.domain.Nature;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.Recognition;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class YydDicNatureRecognition implements Recognition {
	private static final long serialVersionUID = 1L;
	private static final Map<String, Recognition[]> RECOGNITIONS;
	static {
		RECOGNITIONS = new HashMap<>();
		RECOGNITIONS.put("number", new Recognition[] { new RegexRecognition("\\d+(\\.\\d+)?", "number"),
				new RegexRecognition("((零|一|二|三|四|五|六|七|八|九|十)(十|百|千|万|亿|兆)*)+", "number") });
	}
	private Forest[] forests;
	private Set<String> varTypes;

	public YydDicNatureRecognition(Set<String> varTypes, Forest... forests) {
		this.forests = forests;
		this.varTypes = varTypes;
	}

	@Override
	public void recognition(Result result) {
		new UserDicNatureRecognition(forests).recognition(result);
		varTypes.forEach(varType -> Optional.ofNullable(RECOGNITIONS.get(varType)).ifPresent(recognitions -> {
			for (Recognition recognition : recognitions) {
				recognition.recognition(result);
			}
		}));
		reRecognition(result);
	}

	private void reRecognition(Result result) {
		List<Term> terms = new ArrayList<>();
		int termSize = 0;
		for (Term term : result) { // 根据当前场景下的实体词性将分词后与当前场景相关的词提取出来
			for (Forest forest : forests) {
				String[] params = UserDicNatureRecognition.getParams(forest, term.getName());
				Optional.ofNullable(params).map(param -> param[0]).map(natureName -> {
					if (ParserUtils.isKeyword(natureName) || ParserUtils.isCategory(natureName)) {
						Term tm = new Term(term.getName(), term.getOffe(), term.item());
						tm.setNature(new Nature(natureName));
						return tm;
					}
					return null;
				}).ifPresent(tm -> terms.add(tm));
			}
			int size = terms.size(); // 判断是否提取成功，如果成功则忽略该词（因为提取过程中会将该词一并提取），提取失败则判断该词性是否是关键词或实体词，如果是则将词性设置为ignore，否则按原词性提取
			if (termSize == size) {
				Optional.of(term).filter(
						e -> ParserUtils.isKeyword(e.getNatureStr()) || ParserUtils.isCategory(e.getNatureStr()))
						.ifPresent(e -> e.setNature(RegexRecognition.IGNORE_NATURE));
				terms.add(term);
				termSize += 1;
			} else {
				termSize = size;
			}
		} // 替换分词结果
		result.setTerms(terms);
	}
}
