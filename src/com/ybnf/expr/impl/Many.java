package com.ybnf.expr.impl;

import com.ybnf.expr.Expr;

public class Many implements Expr {
	private Expr expr;
	private int min;
	private int max;

	public Many(Expr expr) {
		this(0, expr);
	}

	public Many(int min, Expr expr) {
		this(min, min, expr);
	}

	public Many(int min, int max, Expr expr) {
		this.min = min;
		this.max = max;
		this.expr = expr;
	}

	@Override
	public String expr() {
		StringBuilder sb = new StringBuilder(new Group(expr).expr());
		min = min < 0 ? 0 : min;
		if (min == max) {
			if (min == 0) {
				sb.append("*");
			} else {
				sb.append("{").append(min).append("}");
			}
		} else if (min > max) {
			if (min == 0) {
				sb.append("*");
			} else if (min == 1) {
				sb.append("+");
			} else {
				sb.append("{").append(min).append("}");
			}
		} else {
			sb.append("{").append(min).append(",").append(max).append("}");
		}
		return sb.toString();
	}
}
