package com.ybnf.compiler;

import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.semantic.SemanticCallable;

public interface ICompiler {
	YbnfCompileResult compile(String text);
	
	void setSemanticCallable(SemanticCallable semanticCallable);
}
