package com.ybnf.compiler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ybnf.compiler.Compiler;
import com.ybnf.compiler.Include;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.semantic.SemanticCallable;

import clojure.lang.APersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;

public class YbnfCompiler extends Compiler {
	private String classRootPath;
	private List<Compiler> compilers;
	private SemanticCallable semanticCallable;
	private final static Var INTO = RT.var("clojure.core", "into");
	private ArrayList<?> callables = null;

	public YbnfCompiler(String ybnf) throws Exception {
		super(ybnf);
	}

	@Override
	public void includes() throws Exception {
		if (compilers == null) {
			compilers = new ArrayList<Compiler>();
		}
		Set<String> filenames = getFilenames();
		if (filenames == null)
			return;
		for (String filename : filenames) {
			Compiler compiler = getInclude(convertIncludeFilepath(filename)).compiler();
			compilers.add(compiler);
		}
	}

	@Override
	public String getGrammar() {
		String grammarLang = super.getGrammar();
		StringBuilder grammar = new StringBuilder(grammarLang.trim());
		for (Compiler compiler : compilers) {
			grammar.append("\n").append(compiler.getGrammar());
		}
		return grammar.toString();
	}

	@Override
	public APersistentMap getKeyValue() {
		Object result = super.getKeyValue();
		for (Compiler compiler : compilers) {
			result = INTO.invoke(result, compiler.getKeyValue());
		}
		return (APersistentMap) result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ArrayList<?> getCallable() {
		if (callables == null) {
			synchronized (this) {
				if (callables == null) {
					callables = new ArrayList<>();
					ArrayList calls = super.getCallable();
					if (calls != null) {
						callables.addAll(calls);
					}
					for (Compiler compiler : compilers) {
						calls = compiler.getCallable();
						if (calls != null) {
							callables.addAll(calls);
						}
					}
				}
			}
		}
		return callables;
	}

	private String getClassPath() {
		if (classRootPath == null) {
			classRootPath = this.getClass().getResource("/").getPath();
		}
		return classRootPath;
	}

	public String convertIncludeFilepath(String filepath) {
		if (filepath.startsWith("classpath:")) {
			String[] filenames = filepath.split(":", 2);
			return getClassPath() + filenames[1];
		} else if (filepath.startsWith("file:")) {
			String[] filenames = filepath.split(":", 2);
			return filenames[1];
		} else {
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

	@Override
	public String runCallable(String text) {
		String result = "";
		ArrayList<?> callables = getCallable();
		if (callables == null) {
			return result;
		}
		Map<String, String> params = new HashMap<String, String>();
		synchronized (this) {
			for (Object callable : callables) {
				Object[] objs = (Object[]) callable;
				String rs = call(text, objs);
				if (rs == null) {
					continue;
				}
				StringBuilder var = new StringBuilder("$_call_");
				for (Object object : objs) {
					var.append(object).append("_");
				}
				params.put(var.toString(), rs);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append(";");
		}
		try {
			result = new YbnfCompiler("#YBNF 0.1 utf8;" + sb.toString()).getGrammar();
			System.out.println("======================================================");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String call(String text, Object... args) {
		Object callName = args[0];
		Object[] params = {};
		if (args.length > 1) {
			params = new Object[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				params[i - 1] = args[i];
			}
		}
		if (semanticCallable == null) {
			return "'" + text + "'";
		}
		return semanticCallable.call(text, callName, params);
	}

	@Override
	public void setSemanticCallable(SemanticCallable semanticCallable) {
		if (this.semanticCallable == null) {
			this.semanticCallable = semanticCallable;
		}
	}
}
