package com.ybnf.expr;

import java.util.Map;

public interface ExprService {
	void include(String name, String template) throws Exception;

	Map<String, String> compile(String template, String lang) throws Exception;
}
