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
		varTypes.forEach(varType -> Optional.ofNullable(RECOGNITIONS.get(varType)).ifPresent(recognitions -> {
			for (Recognition recognition : recognitions) {
				recognition.recognition(result);
			}
		}));
		List<Term> terms = new ArrayList<>();
		int termSize = 0;
		for (Term term : result) {
			for (Forest forest : forests) {
				String[] params = UserDicNatureRecognition.getParams(forest, term.getName());
				Optional.ofNullable(params).map(param -> param[0]).map(natureName -> {
					if ("kv".equals(natureName) || natureName.startsWith("c:")) {
						Term tm = new Term(term.getName(), term.getOffe(), term.item());
						tm.setNature(new Nature(natureName));
						return tm;
					}
					return null;
				}).ifPresent(tm -> terms.add(tm));
			}
			int size = terms.size();
			if (termSize == size) {
				Optional.of(term).filter(e -> "kv".equals(e.getNatureStr()) || e.getNatureStr().startsWith("c:"))
						.ifPresent(e -> e.setNature(RegexRecognition.TMP_NATURE));
				terms.add(term);
				termSize += 1;
			} else {
				termSize = size;
			}
		}
		result.setTerms(terms);
	}
}
