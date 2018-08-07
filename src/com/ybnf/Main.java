package com.ybnf;

import java.util.Map.Entry;

import org.ansj.domain.Result;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class Main {
	public static void main(String[] args) throws Exception {
		Forest forest = new Forest();
		forest.add("星期四", new String[] { "kv", "10" });
		Result res = IndexAnalysis.parse("今天星期四", forest);
		new UserDicNatureRecognition(forest).recognition(res);
		System.out.println(res);
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
	}
}
