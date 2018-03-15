package com.ybnf;

import com.ybnf.compiler.lucene.SemanticIntent;
import com.ybnf.compiler.lucene.SemanticService;

public class Main {
	public static void main(String[] args) throws Exception {
		SemanticService service = new SemanticService("music");
		SemanticIntent intent = service.buildIntent("random");
		intent.addTemplate("[[我] 想|要] 听  [首|个]|[一首|一个] (歌|歌曲|音乐)");
		System.out.println(service);
	}
}
