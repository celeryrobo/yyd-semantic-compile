package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class Or implements Expr {
	private Expr first;
	private Expr second;
	private Expr[] exprs;

	public Or(Expr first, Expr second, Expr... exprs) {
		this.first = first;
		this.second = second;
		this.exprs = exprs;
	}

	@Override
	public String expr() {
		StringBuilder sb = new StringBuilder(first.expr());
		sb.append("|").append(second.expr());
		for (Expr expr : exprs) {
			sb.append("|").append(expr.expr());
		}
		return sb.toString();
	}
}
