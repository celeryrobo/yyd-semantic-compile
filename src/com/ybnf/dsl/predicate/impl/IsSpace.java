package com.ybnf.dsl.predicate.impl;

import com.ybnf.dsl.predicate.Predicate;

public class IsSpace implements Predicate {

	@Override
	public boolean satisfy(String value) {
		return Character.isSpaceChar(value.charAt(0));
	}

}
