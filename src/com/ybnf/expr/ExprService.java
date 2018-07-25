package com.ybnf.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ybnf.compiler.lucene.ParserUtils;
import com.ybnf.expr.impl.Group;
import com.ybnf.expr.impl.OneOrMany;
import com.ybnf.expr.impl.Or;
import com.ybnf.expr.impl.Regex;
import com.ybnf.expr.impl.Selectable;
import com.ybnf.expr.impl.Word;

public class ExprService {
	private static final Logger LOG = Logger.getLogger(ExprService.class.getSimpleName());
	public static final ExprService DSL_INCLUDE_SERVICE = new ExprService();
	static {
		// 数字
		Expr number = new Or(new Word("零"), new Word("一"), new Word("二"), new Word("两"), new Word("三"), new Word("四"),
				new Word("五"), new Word("六"), new Word("七"), new Word("八"), new Word("九"), new Word("十"), new Word("百"),
				new Word("千"), new Word("万"), new Word("亿"), new Regex("\\d"));
		number = new OneOrMany(number);
		number = new Group(number,
				new Selectable(new Group(new Group(new Or(new Word("点"), new Word("\\."))), number)));
		DSL_INCLUDE_SERVICE.include("number", number);
		DSL_INCLUDE_SERVICE.include("enChar", new Regex("\\w"));
		DSL_INCLUDE_SERVICE.include("cnChar", new Regex("\\p{script=Han}"));
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

	public Map<String, String> compile(String template, String lang) throws Exception {
		List<String> varNames = new ArrayList<>();
		Map<String, String> varCommonNames = new HashMap<>();
		Map<String, String> params = new HashMap<>();
		StringTokenizer tokenizer = new StringTokenizer(template, " ");
		String varName = null;
		int beginIndex = 0;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$")) {
				String oldVarName = varName;
				varName = token.substring(1);
				int nameLength = varName.length();
				switch (varName.substring(nameLength - 1)) {
				case "+":
				case "*": {
					varName = varName.substring(0, nameLength - 1);
					varCommonNames.put(varName, oldVarName);
				}
				default:
					varNames.add(varName);
					break;
				}
			} else if (!lang.contains(token)) {
				throw new Exception("Semantic Match Failture, Keyword is not exsit!");
			} else {
				if (varName != null) {
					params.put(varName, lang.substring(beginIndex, lang.indexOf(token, beginIndex)));
					varName = null;
				}
				beginIndex = lang.indexOf(token, beginIndex) + token.length();
			}
		}
		if (varName != null) {
			params.put(varName, lang.substring(beginIndex, lang.length()));
		}
		varCommonNames.forEach((k, v) -> {
			if (!includes.containsKey(k)) {
				String param = params.get(k);
				Expr expr = null;
				if (v != null && null != (expr = includes.get(v))) {
					String[] arr = param.split(expr.expr());
					StringJoiner sj = new StringJoiner("|");
					for (String w : arr) {
						if (!"".equals(w)) {
							sj.add(w);
						}
					}
					param = sj.toString();
					try {
						expr = ParserUtils.generate(param, includes);
					} catch (Exception e) {
						expr = new Regex(".+");
					}
				} else {
					expr = new Word(param);
				}
				includes.put(k, expr);
			}
		});
		Expr expr = ParserUtils.generate(template, includes);
		if (expr == null) {
			throw new Exception("Semantic Match Failture !");
		}
		String regex = expr.expr();
		LOG.info("REGEX: " + regex);
		return parse(regex, lang, varNames);
	}

	private Map<String, String> parse(String regex, String lang, List<String> varNames) throws Exception {
		LOG.info("Lang: " + lang);
		Map<String, String> map = new HashMap<>();
		Matcher matcher = Pattern.compile(regex).matcher(lang);
		if (matcher.find()) {
			String sentence = matcher.group();
			LOG.info("Sentence: " + sentence);
			float score = ParserUtils.distanceScore(lang, sentence);
			if (score < 0.5F) {
				throw new Exception("Semantic Match Failture, Distance Score < 0.5!");
			}
			for (String varName : varNames) {
				map.put(varName, matcher.group(varName));
			}
			return map;
		}
		throw new Exception("Semantic Match Failture !");
	}
}
