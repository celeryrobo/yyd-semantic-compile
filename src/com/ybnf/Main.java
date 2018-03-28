package com.ybnf;

import java.util.LinkedList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

public class Main {
	private static List<Term> text(String lang, Result r) {
		List<Term> strs = new LinkedList<>();
		int curSize = 0, curIndex = 0;
		for (Term t : r) {
			String s = t.getRealName();
			curIndex = lang.indexOf(s, curSize - 1);
			if(curIndex >= curSize) {
				System.out.println(s);
				curSize += s.length();
				strs.add(t);
			}
		}
		return strs;
	}
	
	public static void main(String[] args) throws Exception {
		Forest forest = new Forest();
		Library.insertWord(forest, new Value("我想听", "kv", "1"));
		Library.insertWord(forest, new Value("想听", "kv", "1"));
		Library.insertWord(forest, new Value("听", "kv", "1"));
		Library.insertWord(forest, new Value("的", "kv", "1"));
		Library.insertWord(forest, new Value("床前明月光", "txt", "1"));
		Library.insertWord(forest, new Value("明月", "txt", "1"));
		Library.insertWord(forest, new Value("李白", "txt", "1"));
		String lang = "我想听李白的床前明月光";
		Result res = IndexAnalysis.parse(lang, forest);
		new UserDicNatureRecognition(forest).recognition(res);
		System.out.println(res);
		System.out.println(text(lang, res));;
	}
}
