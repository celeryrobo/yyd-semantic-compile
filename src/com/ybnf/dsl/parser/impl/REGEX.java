package com.ybnf.dsl.parser.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ybnf.dsl.domain.Result;
import com.ybnf.dsl.parser.Parser;

public class REGEX implements Parser {
	private Pattern pattern;

	public REGEX(String regex) {
		if (!regex.startsWith("^")) {
			regex = "^" + regex;
		}
		pattern = Pattern.compile(regex);
	}

	@Override
	public Result parse(String target) {
		Matcher matcher = pattern.matcher(target);
		if (!matcher.find()) {
			return Result.fail();
		}
		return new CHARACTER(matcher.group().length()).parse(target);
	}

}
