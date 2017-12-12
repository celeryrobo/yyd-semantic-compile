package com.ybnf;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.impl.JCompiler;
import com.ybnf.compiler.utils.CompilerUtils;

public class Main {
	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("#YBNF 1.0 utf8;");
		sb.append("service commont;");
		sb.append("root $main;");
		sb.append("$main = 你好;");
		ICompiler compiler = new JCompiler(sb.toString());
		System.out.println(compiler.compile("你好"));
		System.out.println(CompilerUtils.hashmap(CompilerUtils.keyword("test"), "xxxx"));
	}
}
