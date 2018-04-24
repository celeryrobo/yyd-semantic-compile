package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class Selectable extends Many {
	public Selectable(Expr expr) {
		super(0, 1, expr);
	}
}
