package com.ybnf.compiler.impl;

import com.ybnf.compiler.Compiler;
import com.ybnf.compiler.Include;

public class OriginalInclude extends Include {

	@Override
	public Compiler compiler() throws Exception {
		Compiler compiler = new YbnfCompiler("#YBNF 1.0 utf8;");
		compiler.mergeGrammar(this.readContent());
		return compiler;
	}

	@Override
	public String readContent() throws Exception {
		StringBuilder sb = new StringBuilder("<_yyd_han_> = #'\\p{script=Han}'\n");
		sb.append("<_yyd_digital_> = #'\\d'\n").append("<_yyd_num_> = _yyd_digital_+");
		return sb.toString();
	}

}
