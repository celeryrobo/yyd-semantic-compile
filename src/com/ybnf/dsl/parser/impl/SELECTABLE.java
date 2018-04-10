package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class SELECTABLE implements Parser {
	private Parser parser;

	public SELECTABLE(Parser parser) {
		this.parser = parser;
	}

	@Override
	public Result parse(String target) {
		Result rs = parser.parse(target);
		return rs.isSucceeded() ? rs : Result.succeed("", target);
	}
}
