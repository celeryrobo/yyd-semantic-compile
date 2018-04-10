package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.predicate.impl.IsWord;

public class WORD extends SAT {
	public WORD(String value) {
		super(new IsWord(value), new CHARACTER(value.length()));
	}
}
