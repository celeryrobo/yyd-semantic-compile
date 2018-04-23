package com.ybnf.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ybnf.dsl.domain.NamedResult;
import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;
import com.ybnf.dsl.parser.impl.DIGIT;
import com.ybnf.dsl.parser.impl.GROUP;
import com.ybnf.dsl.parser.impl.NamedParser;
import com.ybnf.dsl.parser.impl.ORR;
import com.ybnf.dsl.parser.impl.OneOrMany;
import com.ybnf.dsl.parser.impl.REGEX;
import com.ybnf.dsl.parser.impl.SELECTABLE;
import com.ybnf.dsl.parser.impl.WORD;

public class DslService {
	public static final DslService DSL_INCLUDE_SERVICE = new DslService();
	static {
		// 数字
		Parser number = new ORR(new WORD("零"), new WORD("一"), new WORD("二"), new WORD("两"), new WORD("三"),
				new WORD("四"), new WORD("五"), new WORD("六"), new WORD("七"), new WORD("八"), new WORD("九"), new WORD("九"),
				new WORD("十"), new WORD("百"), new WORD("千"), new WORD("万"), new WORD("亿"), new DIGIT());
		number = new OneOrMany(number);
		number = new GROUP(number, new SELECTABLE(new GROUP(new WORD("."), number)));
		Parser fuzzyWord = new ORR(new REGEX("\\p{script=Han}"), new REGEX("\\w"));
		DSL_INCLUDE_SERVICE.include("number", number);
		DSL_INCLUDE_SERVICE.include("fuzzyWord", fuzzyWord);
	}
	private Map<String, Parser> includes;

	public DslService() {
		this(DSL_INCLUDE_SERVICE);
	}

	public DslService(DslService service) {
		if (service == null) {
			includes = new HashMap<>();
		} else {
			includes = new HashMap<>(service.includes);
		}
	}

	public boolean containsKey(String key) {
		return includes.containsKey(key);
	}

	public DslService include(String name, Parser parser) {
		includes.put(name, parser);
		return this;
	}

	public DslService or(String name, Parser first, Parser second, Parser... parsers) {
		return include(name, new NamedParser(name, new ORR(first, second, parsers)));
	}

	public DslService map(String name, Parser parser) {
		return include(name, new NamedParser(name, parser));
	}

	public DslService assign(String name, String parserName) throws Exception {
		Parser parser = includes.get(parserName);
		if (parser == null) {
			throw new Exception("$" + parserName + " is not exsit!");
		}
		return include(name, new NamedParser(name, parser));
	}

	public Map<String, String> compile(String template, String lang) throws Exception {
		List<Parser> parsers = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		List<String> tokens = new ArrayList<>();
		while (tokenizer.hasMoreTokens()) {
			tokens.add(tokenizer.nextToken());
		}
		int start = 0, end = 0, size = tokens.size();
		for (int i = 0; i < size; i++) {
			String token = tokens.get(i);
			if (token.startsWith("$")) {
				String varName = token.substring(1);
				Parser parser = null;
				if (varName.endsWith("*")) {
					parser = includes.get("fuzzyWord");
				} else {
					parser = includes.get(varName);
				}
				if (parser == null) {
					throw new Exception(token + " is not exsit!");
				}
				if (varName.endsWith("*")) {
					// 解析通配变量
					if (size == 1 || i == size - 1) {
						// 集合内只有一个通配变量或者通配变量是最后一个元素时设置为无约束通配
						parsers.add(new NamedParser(varName, new OneOrMany(parser)));
					} else {
						// 根据条件计算通配字符串的长度
						if (i == 0) {
							// 当通配变量为第一个元素时，start不变并为0
							String afterStr = tokens.get(i + 1);
							end = lang.indexOf(afterStr, end);
						} else {
							// 当通配变量位于中间位置时，获取前面的字符串计算start，并获取后面得字符串计算end
							String beforeStr = tokens.get(i - 1);
							String afterStr = tokens.get(i + 1);
							start = lang.indexOf(beforeStr, start) + beforeStr.length();
							end = lang.indexOf(afterStr, end);
						}
						parsers.add(new NamedParser(varName, new OneOrMany(end - start, parser)));
					}
				} else {
					parsers.add(new NamedParser(varName, parser));
				}
			} else {
				parsers.add(new WORD(token));
			}
		}
		Parser[] arr = new Parser[parsers.size() - 1];
		Parser first = parsers.get(0);
		for (int i = 0; i < arr.length; i++) {
			arr[i] = parsers.get(i + 1);
		}
		return parse(new GROUP(first, arr).parse(lang));
	}

	private Map<String, String> parse(Result result) throws Exception {
		if (!result.isSucceeded()) {
			throw new Exception("dsl analyze parser fail!");
		}
		Map<String, String> map = new HashMap<>();
		if (result instanceof NamedResult) {
			NamedResult namedResult = (NamedResult) result;
			map.put(namedResult.getName(), namedResult.getRecognized());
		}
		for (Result rs : result.getResults()) {
			map.putAll(parse(rs));
		}
		return map;
	}
}
