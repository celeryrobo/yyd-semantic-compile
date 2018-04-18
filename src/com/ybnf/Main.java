package com.ybnf;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ybnf.dsl.DslService;
import com.ybnf.dsl.parser.Parser;
import com.ybnf.dsl.parser.impl.DIGIT;
import com.ybnf.dsl.parser.impl.GROUP;
import com.ybnf.dsl.parser.impl.ORR;
import com.ybnf.dsl.parser.impl.OneOrMany;
import com.ybnf.dsl.parser.impl.SELECTABLE;
import com.ybnf.dsl.parser.impl.WORD;

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
		Parser number = new ORR(new WORD("零"), new WORD("一"), new WORD("二"), new WORD("三"), new WORD("四"),
				new WORD("五"), new WORD("六"), new WORD("七"), new WORD("八"), new WORD("九"), new WORD("九"), new WORD("十"),
				new WORD("百"), new WORD("千"), new WORD("万"), new WORD("亿"), new DIGIT());
		number = new OneOrMany(number);
		number = new GROUP(number, new SELECTABLE(new GROUP(new WORD("."), number)));
		Parser orParser = new ORR(new WORD("我想听"), new WORD("刘德华"), new WORD("的"), new WORD("冰雨"));
		DslService service = new DslService();
		service.include("number", number);
		Object o = service.assign("num", "number").map("singer", orParser)
				.map("song", orParser).compile("我想听 $num 首 $singer 的 $song", "我想听101.11首刘德华的冰雨呀");
		System.out.println(o);
		System.out.println(System.currentTimeMillis() - start);
		
		Matcher matcher = Pattern.compile("我想听(.+我想听|我想听|李白|的|静夜思)的(.*我想听|我想听|李白|的|静夜思)").matcher("我想听我想听李白的静夜思");
		if(matcher.find()) {
			System.out.println(matcher.group());
		}
	}
}
