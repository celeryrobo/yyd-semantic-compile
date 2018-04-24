package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class Group implements Expr {
	protected Expr first;
	protected Expr[] exprs;

	public Group(Expr first, Expr... exprs) {
		this.first = first;
		this.exprs = exprs;
	}

	@Override
	public String expr() {
		StringBuilder sb = new StringBuilder("(").append(first.expr());
		for (Expr expr : exprs) {
			sb.append(expr.expr());
		}
		return sb.append(")").toString();
	}
}
