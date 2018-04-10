package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class ZeroOrMany extends OneOrMany {
	public ZeroOrMany(Parser parser) {
		super(Integer.MAX_VALUE, parser);
	}

	public ZeroOrMany(int max, Parser parser) {
		super(max, parser);
	}

	@Override
	public Result parse(String target) {
		Result rs = super.parse(target);
		return rs.isSucceeded() ? rs : Result.succeed("", target);
	}
}
