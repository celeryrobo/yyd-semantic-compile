package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class CHARACTER implements Parser {
	private int size;

	public CHARACTER() {
		this(1);
	}

	public CHARACTER(int size) {
		this.size = size;
	}

	@Override
	public Result parse(String target) {
		if (target == null || target.isEmpty() || target.length() < size) {
			return Result.fail();
		}
		return Result.succeed(target.substring(0, size), target.substring(size));
	}
}
