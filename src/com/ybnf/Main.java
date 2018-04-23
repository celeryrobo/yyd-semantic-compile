package com.ybnf;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ybnf.dsl.DslService;

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
		DslService service = new DslService();
		Object o = service.compile("记住你叫 $name* 喔", "记住你叫小勇喔");
		System.out.println(o);
		System.out.println(System.currentTimeMillis() - start);
	}
}
