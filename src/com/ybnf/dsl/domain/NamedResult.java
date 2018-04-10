package com.ybnf.dsl.domain;

public class NamedResult extends Result {
	private String name;

	public NamedResult(String name, Result result) {
		super(result);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
