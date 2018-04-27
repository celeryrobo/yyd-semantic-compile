package com.ybnf.compiler.lucene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
		RECOGNITIONS.put("number", new Recognition[] { new RegexRecognition("\\d+(\\.\\d+){0,1}", "number"),
				new RegexRecognition("((零|一|二|三|四|五|六|七|八|九|十)(十|百|千|万|亿|兆)*)+", "number") });
	}
	private UserDicNatureRecognition recognition;
	private Forest[] forests;
	private Set<String> varTypes;

	public YydDicNatureRecognition(Set<String> varTypes, Forest... forests) {
		recognition = new UserDicNatureRecognition(forests);
		this.forests = forests;
		this.varTypes = varTypes;
	}

	@Override
	public void recognition(Result result) {
		for (String varType : varTypes) {
			Recognition[] recognitions = RECOGNITIONS.get(varType);
			if (recognitions != null) {
				for (Recognition recognition : recognitions) {
					recognition.recognition(result);
				}
			}
		}
		recognition.recognition(result);
		REC: for (Iterator<Term> it = result.iterator(); it.hasNext();) {
			Term term = it.next();
			if (!"kv".equals(term.getNatureStr())) {
				continue REC;
			}
			for (int i = forests.length - 1; i > -1; i--) {
				String[] params = UserDicNatureRecognition.getParams(forests[i], term.getName());
				if (params != null) {
					continue REC;
				}
			}
			it.remove();
		}
	}
}
