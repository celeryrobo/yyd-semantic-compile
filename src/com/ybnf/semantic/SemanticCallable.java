package com.ybnf.semantic;

public interface SemanticCallable {
	String call(String text, Object callName, Object... args);
}
