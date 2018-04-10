package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.predicate.impl.IsDigit;

public class DIGIT extends SAT {

	public DIGIT() {
		super(new IsDigit(), new CHARACTER());
	}

}
