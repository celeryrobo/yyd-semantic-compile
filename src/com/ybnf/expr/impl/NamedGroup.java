package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class NamedGroup extends Group {
	private String name;

	public NamedGroup(String name, Expr first, Expr... exprs) {
		super(first, exprs);
		this.name = name;
	}

	@Override
	public String expr() {
		StringBuilder sb = new StringBuilder("(?<");
		sb.append(name).append(">").append(first.expr());
		for (Expr expr : exprs) {
			sb.append(" ").append(expr.expr());
		}
		return sb.append(")").toString();
	}
}
