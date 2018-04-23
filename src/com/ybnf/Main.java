package com.ybnf;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import com.ybnf.compiler.lucene.RegexRecognition;

class U {
	private String service;
	private String intent;
	private String tpl;

	public U(String service, String intent, String tpl) {
		this.service = service;
		this.intent = intent;
		this.tpl = tpl;
	}

	public String getService() {
		return service;
	}

	public String getIntent() {
		return intent;
	}

	public String getTpl() {
		return tpl;
	}

	@Override
	public String toString() {
		return "{service=" + service + ", intent=" + intent + ", tpl=" + tpl + "}";
	}
}

public class Main {
	public static void main(String[] args) throws Exception {
		List<U> lst = new LinkedList<>();
		lst.add(new U("music", "random", "给我唱首 $singer 的歌"));
		lst.add(new U("music", "bySingerAndSong", "给我唱首 $singer 的 $song"));
		lst.add(new U("story", "play", "给我讲个 $storyName 的故事"));
		lst.add(new U("story", "byTag", "给我讲个 $storyTag 的故事"));
		Map<String, Map<String, List<U>>> r = lst.stream().collect(
				Collectors.groupingBy(U::getService, Collectors.groupingBy(U::getIntent, Collectors.toList())));
		System.out.println(r);

		long start = System.currentTimeMillis();
		Forest forest = DicLibrary.get();
		for (int i = 0; i < 10; i++) {
			Library.insertWord(forest, new Value("" + i, "num", "1"));
		}
		Library.insertWord(forest, new Value("分钟", "q", "1"));
		Map<String, String[]> m = forest.toMap();
		for (Entry<String, String[]> e : m.entrySet()) {
			System.out.println(e.getKey() + ":" + e.getValue()[0] + " - " + e.getValue()[1]);
		}
		Result res = IndexAnalysis.parse("前进一千二百零三万九千三百八十四分钟", forest);
		new RegexRecognition("\\d+(\\.\\d+){0,1}", "num").recognition(res);
		new RegexRecognition("((零|一|二|三|四|五|六|七|八|九|十)(十|百|千|万|亿|兆)*)+", "num").recognition(res);
		new UserDicNatureRecognition(forest).recognition(res);
		System.out.println(res);
		System.out.println(System.currentTimeMillis() - start);
	}
}
