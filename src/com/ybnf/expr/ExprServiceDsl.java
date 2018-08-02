package com.ybnf.expr;

import java.util.Map;
import java.util.logging.Logger;

import com.ybnf.compiler.lucene.ParserUtils;
import com.yyd.dsl.DslService;
import com.yyd.dsl.parser.Parser;
import com.yyd.dsl.parser.impl.AND;
import com.yyd.dsl.parser.impl.OR;
import com.yyd.dsl.parser.impl.OneOrMany;
import com.yyd.dsl.parser.impl.REGEX;
import com.yyd.dsl.parser.impl.SELECTABLE;
import com.yyd.dsl.parser.impl.WORD;

public class ExprServiceDsl implements ExprService {
	private static final Logger LOG = Logger.getLogger(ExprServiceDsl.class.getSimpleName());
	public static final DslService DSL_INCLUDE_SERVICE = new DslService();
	static {
		try {
			Parser number = DSL_INCLUDE_SERVICE.build("零|一|二|两|三|四|五|六|七|八|九|十|百|千|万|亿");
			number = new OR(new REGEX("\\d+"), new OneOrMany(number));
			number = new AND(number, new SELECTABLE(new AND(new OR(new WORD("点"), new WORD(".")), number)));
			DSL_INCLUDE_SERVICE.include("number", number);
			DSL_INCLUDE_SERVICE.include("enChar", new REGEX("\\w"));
			DSL_INCLUDE_SERVICE.include("cnChar", new REGEX("\\p{script=Han}"));
		} catch (Exception e) {
		}
	}
	private DslService dsl;

	public ExprServiceDsl() {
		dsl = new DslService(DSL_INCLUDE_SERVICE);
	}

	@Override
	public void include(String name, String template) throws Exception {
		dsl.include(name, template);
	}

	@Override
	public Map<String, String> compile(String template, String lang) throws Exception {
		Map<String, String> result = dsl.map("lang", template).compile("$lang", lang);
		if (result == null) {
			throw new Exception("Semantic Match Failture !");
		}
		String sentence = result.remove("lang");
		LOG.info("REGEX: " + template);
		LOG.info("Lang: " + lang);
		LOG.info("Sentence: " + sentence);
		float score = ParserUtils.distanceScore(lang, sentence);
		if (score < 0.5F) {
			throw new Exception("Semantic Match Failture, Distance Score < 0.5!");
		}
		return result;
	}

}
