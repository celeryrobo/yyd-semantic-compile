package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class OneOrMany implements Parser {
	private int max;
	private Parser parser;

	public OneOrMany(Parser parser) {
		this(Integer.MAX_VALUE, parser);
	}

	public OneOrMany(int max, Parser parser) {
		this.max = max;
		this.parser = parser;
	}

	@Override
	public Result parse(String target) {
		Result rs = parser.parse(target);
		return rs.isSucceeded() ? process(rs, 1) : Result.fail();
	}

	private Result process(Result result, int count) {
		if (count >= max) {
			return result;
		}
		Result rs = parser.parse(result.getRemaining());
		return rs.isSucceeded() ? process(Result.concat(result, rs), count + 1) : result;
	}
}
