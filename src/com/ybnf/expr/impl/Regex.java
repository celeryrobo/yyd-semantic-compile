package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class Regex implements Expr {
	private String regex;

	public Regex(String regex) {
		this.regex = regex;
	}

	@Override
	public String expr() {
		return regex;
	}
}
