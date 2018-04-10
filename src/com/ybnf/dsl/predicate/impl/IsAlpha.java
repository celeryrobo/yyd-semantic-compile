package com.ybnf.dsl.predicate.impl;

import com.ybnf.dsl.predicate.Predicate;

public class IsAlpha implements Predicate {

	@Override
	public boolean satisfy(String value) {
		return Character.isAlphabetic(value.charAt(0));
	}

}
