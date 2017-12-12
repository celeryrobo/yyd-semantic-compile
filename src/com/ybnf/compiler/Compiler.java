package com.ybnf.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.semantic.SemanticCallable;

import ybnf.compiler;

public abstract class Compiler extends compiler implements ICompiler {
	private Set<String> filenames = null;
	private String service = null;
	private String version = null;
	private String charset = null;
	private Map<String, ArrayList<String>> header = null;
	
	@SuppressWarnings("unchecked")
	public Compiler(String ybnf) throws Exception {
		super(ybnf);
		if(isFailure()) {
			throw new Exception(getFailure());
		}
		header = (Map<String, ArrayList<String>>) this.getHeader();
		if (header.containsKey("filename")) {
			filenames = new HashSet<String>();
			for (String filename : header.get("filename")) {
				filenames.add(filename.trim());
			}
		}
		if (header.containsKey("service")) {
			ArrayList<String> services = header.get("service");
			if (services != null && services.size() > 0) {
				service = services.get(0);
			}
		}
		if (header.containsKey("version")) {
			ArrayList<String> versions = header.get("version");
			if (versions != null && versions.size() > 0) {
				version = versions.get(0);
			}
		}
		if (header.containsKey("charset")) {
			ArrayList<String> charsets = header.get("charset");
			if (charsets != null && charsets.size() > 0) {
				charset = charsets.get(0);
			}
		}
		includes();
	}
	
	public Set<String> getFilenames() {
		return filenames;
	}

	public String getService() {
		return service;
	}

	public String getVersion() {
		return version;
	}

	public String getCharset() {
		return charset;
	}
	
	public abstract void setSemanticCallable(SemanticCallable semanticCallable);
	
	public abstract void includes () throws Exception;

	public abstract YbnfCompileResult compile(String text);
}
