package com.ybnf.dsl.predicate.impl;

import com.ybnf.dsl.predicate.Predicate;

public class IsDigit implements Predicate {

	@Override
	public boolean satisfy(String value) {
		return Character.isDigit(value.charAt(0));
	}

}
