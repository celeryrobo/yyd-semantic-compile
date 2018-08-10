package com.ybnf.compiler;

import com.ybnf.compiler.beans.YbnfCompileResult;

public interface ICompiler {
	YbnfCompileResult compile(String text) throws Exception;
}
