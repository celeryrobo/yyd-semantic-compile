package com.ybnf.compiler.impl;

import java.util.Map;
import java.util.Set;

import com.ybnf.compiler.Compiler;
import com.ybnf.compiler.Include;
import com.ybnf.compiler.beans.YbnfCompileResult;

public class YbnfCompiler extends Compiler {
	private String classRootPath;

	public YbnfCompiler(String ybnf) throws Exception {
		super(ybnf);
	}

	@Override
	public void includes() throws Exception {
		Set<String> filenames = getFilenames();
		if (filenames == null)
			return;
		for (String filename : filenames) {
			Compiler compiler = getInclude(convertIncludeFilepath(filename)).compiler();
			mergeGrammar(compiler.getGrammar().trim());
			mergeKeyValue(compiler.getKeyValue());
		}
	}
	
	private String getClassPath() {
		if(classRootPath == null) {
			classRootPath = this.getClass().getResource("/").getPath();
		}
		return classRootPath;
	}
	
	public String convertIncludeFilepath(String filepath) {
		if(filepath.startsWith("classpath:")) {
			String[] filenames = filepath.split(":", 2);
			return getClassPath() + filenames[1];
		}else if(filepath.startsWith("file:")) {
			String[] filenames = filepath.split(":", 2);
			return filenames[1];
		}else {
			return filepath;
		}
	}

	private Include getInclude(String filename) throws Exception {
		switch (filename) {
		case "original.ybnf":
			return new OriginalInclude();
		default:
			return new LocalInclude(filename, getCharset());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public YbnfCompileResult compile(String text) {
		Map<String, Map<String, String>> result = (Map<String, Map<String, String>>) execCompile(text);
		Map<String, String> objects = (Map<String, String>) result.get("objects");
		Map<String, String> slots = (Map<String, String>) result.get("slots");
		return new YbnfCompileResult(text, getVersion(), getCharset(), getService(), objects, slots);
	}
}
