package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.NamedResult;
import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class NamedParser implements Parser {
	private String name;
	private Parser parser;

	public NamedParser(String name, Parser parser) {
		this.name = name;
		this.parser = parser;
	}

	@Override
	public Result parse(String target) {
		return new NamedResult(name, parser.parse(target));
	}
}
