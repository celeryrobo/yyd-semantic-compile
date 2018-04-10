package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class GROUP implements Parser {
	private Parser parser;

	public GROUP(Parser first, Parser... parsers) {
		int size = parsers.length;
		for (int i = size - 1; i >= 0; i--) {
			if (i == size - 1) {
				parser = parsers[i];
			} else {
				parser = new AND(parsers[i], parser);
			}
		}
		if (size > 0) {
			parser = new AND(first, parser);
		} else {
			parser = first;
		}
	}

	@Override
	public Result parse(String target) {
		return parser.parse(target);
	}

}
