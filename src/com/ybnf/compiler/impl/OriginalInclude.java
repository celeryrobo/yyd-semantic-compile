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
		StringBuilder sb = new StringBuilder();
		sb.append("<_yyd_han_> = #'\\p{script=Han}'\n<_yyd_char_> = #'\\w'\n<_yyd_ch_> = _yyd_han_|_yyd_char_\n");
		sb.append("<_yyd_wstr_> = _yyd_han_+\n<_yyd_str_> = _yyd_char_+\n<_yyd_string_> = _yyd_ch_+\n");
		sb.append("<_yyd_digital_> = #'\\d'\n<_yyd_num_> = _yyd_digital_+\n");
		return sb.toString();
	}

}
