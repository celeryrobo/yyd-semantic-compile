package com.ybnf.dsl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ybnf.dsl.domain.NamedResult;
import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;
import com.ybnf.dsl.parser.impl.GROUP;
import com.ybnf.dsl.parser.impl.NamedParser;
import com.ybnf.dsl.parser.impl.ORR;
import com.ybnf.dsl.parser.impl.WORD;

public class DslService {
	private Map<String, Parser> includes;

	public DslService() {
		includes = new HashMap<>();
	}
	
	public DslService(DslService service){
		includes = new HashMap<>(service.includes);
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
		List<Parser> parsers = new LinkedList<>();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				Parser parser = includes.get(token.substring(1));
				if (parser == null) {
					throw new Exception(token + " is not exsit!");
				}
				parsers.add(parser);
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
		if(!result.isSucceeded()) {
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
