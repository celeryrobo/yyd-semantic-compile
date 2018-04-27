package com.ybnf.compiler.lucene;

import java.util.Iterator;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.Recognition;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class YydDicNatureRecognition implements Recognition {
	private static final long serialVersionUID = 1L;
	private UserDicNatureRecognition recognition;
	private Forest[] forests;

	public YydDicNatureRecognition(Forest... forests) {
		recognition = new UserDicNatureRecognition(forests);
		this.forests = forests;
	}

	@Override
	public void recognition(Result result) {
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
