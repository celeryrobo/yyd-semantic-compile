package com.ybnf.semantic;

import com.ybnf.compiler.beans.AbstractSemanticResult;
import com.ybnf.compiler.beans.YbnfCompileResult;

public interface Semantic<T extends AbstractSemanticResult> {
	public T handle(YbnfCompileResult ybnfCompileResult);
}
