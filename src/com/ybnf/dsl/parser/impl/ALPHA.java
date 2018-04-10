package com.ybnf.dsl.parser.impl;

import com.ybnf.dsl.predicate.impl.IsAlpha;

public class ALPHA extends SAT {
	public ALPHA() {
		super(new IsAlpha(), new CHARACTER());
	}
}
