package com.ybnf.compiler;

import com.ybnf.compiler.impl.YbnfCompiler;

public abstract class Include {

	public Compiler compiler() throws Exception {
		String grammar = readContent();
		return new YbnfCompiler(grammar);
	}

	public abstract String readContent() throws Exception;
}
