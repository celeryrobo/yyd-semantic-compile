package com.ybnf.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ybnf.expr.impl.Group;
import com.ybnf.expr.impl.NamedGroup;
import com.ybnf.expr.impl.OneOrMany;
import com.ybnf.expr.impl.Or;
import com.ybnf.expr.impl.Regex;
import com.ybnf.expr.impl.Selectable;
import com.ybnf.expr.impl.Word;
import com.ybnf.expr.impl.ZeroOrMany;

public class ExprService {
	private static final Logger LOG = Logger.getLogger(ExprService.class.getSimpleName());
	public static final ExprService DSL_INCLUDE_SERVICE = new ExprService();
	static {
		// 数字
		Expr number = new Or(new Word("零"), new Word("一"), new Word("二"), new Word("两"), new Word("三"), new Word("四"),
				new Word("五"), new Word("六"), new Word("七"), new Word("八"), new Word("九"), new Word("九"), new Word("十"),
				new Word("百"), new Word("千"), new Word("万"), new Word("亿"), new Regex("\\d"));
		number = new OneOrMany(number);
		number = new Group(number, new Selectable(new Group(new Word("\\."), number)));
		Expr fuzzyWord = new Regex(".");
		DSL_INCLUDE_SERVICE.include("number", number);
		DSL_INCLUDE_SERVICE.include("fuzzyWord", fuzzyWord);
	}
	private Map<String, Expr> includes;

	public ExprService() {
		this(DSL_INCLUDE_SERVICE);
	}

	public ExprService(ExprService service) {
		if (service == null) {
			includes = new HashMap<>();
		} else {
			includes = new HashMap<>(service.includes);
		}
	}

	public boolean containsKey(String key) {
		return includes.containsKey(key);
	}

	public ExprService include(String name, Expr expr) {
		includes.put(name, expr);
		return this;
	}

	private String buildRegexExpr(String template, List<String> varNames) throws Exception {
		List<Expr> parsers = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				String varName = token.substring(1);
				Expr parser = null;
				int length = varName.length();
				switch (varName.substring(length - 1)) {
				case "*":
					parser = new ZeroOrMany(includes.get("fuzzyWord"));
					varName = varName.substring(0, length - 1);
					break;
				case "+":
					parser = new OneOrMany(includes.get("fuzzyWord"));
					varName = varName.substring(0, length - 1);
					break;
				default:
					parser = includes.get(varName);
					break;
				}
				if (parser == null) {
					throw new Exception(token + " is not exsit!");
				}
				parsers.add(new NamedGroup(varName, parser));
				varNames.add(varName);
			} else {
				parsers.add(new Word(token));
			}
		}
		Expr[] arr = new Expr[parsers.size() - 1];
		Expr first = parsers.get(0);
		for (int i = 0; i < arr.length; i++) {
			arr[i] = parsers.get(i + 1);
		}
		return new Group(first, arr).expr();
	}

	public Map<String, String> compile(String template, String lang) throws Exception {
		List<String> varNames = new ArrayList<>();
		String regex = buildRegexExpr(template, varNames);
		LOG.info("REGEX: " + regex);
		return parse(regex, lang, varNames);
	}

	private Map<String, String> parse(String regex, String lang, List<String> varNames) throws Exception {
		LOG.info("Lang: " + lang);
		Map<String, String> map = new HashMap<>();
		Matcher matcher = Pattern.compile(regex).matcher(lang);
		if (matcher.find()) {
			LOG.info("Sentence: " + matcher.group());
			for (String varName : varNames) {
				map.put(varName, matcher.group(varName));
			}
			return map;
		}
		throw new Exception("Semantic Match Failture !");
	}
}
