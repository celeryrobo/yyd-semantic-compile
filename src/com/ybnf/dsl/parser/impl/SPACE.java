package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.predicate.impl.IsSpace;

public class SPACE extends SAT {
	public SPACE() {
		super(new IsSpace(), new CHARACTER());
	}
}
