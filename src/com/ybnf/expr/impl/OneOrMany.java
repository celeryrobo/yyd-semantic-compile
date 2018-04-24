package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class OneOrMany extends Many {
	public OneOrMany(Expr expr) {
		super(1, 0, expr);
	}
}
