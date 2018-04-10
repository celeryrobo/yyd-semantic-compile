package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class ORR implements Parser {
	private Parser parser;

	public ORR(Parser first, Parser second, Parser... parsers) {
		parser = new OR(second, first);
		for (Parser par : parsers) {
			parser = new OR(par, parser);
		}
	}

	@Override
	public Result parse(String target) {
		return parser.parse(target);
	}
}
