package com.ybnf.compiler.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ybnf.compiler.impl.JCompiler;
import com.ybnf.compiler.impl.OriginalInclude;
import com.ybnf.compiler.utils.StructUtils.CallableStruct;
import com.ybnf.compiler.utils.StructUtils.VarnameStruct;
import com.ybnf.semantic.SemanticCallable;

import clojure.lang.IPersistentMap;
import instaparse.core.Parser;

public class YbnfStruct {
	private String version;
	private String charset;
	private String service;
	private Set<Object> includes;
	private List<CallableStruct> callableStructs;
	private String main;
	private String body;
	private SemanticCallable semanticCallable;
	private List<YbnfStruct> ybnfStructs;
	private Map<String, VarnameStruct> kvs;

	private Object grammarSchema = null;

	public YbnfStruct() {
		includes = new HashSet<>();
		callableStructs = new LinkedList<>();
		ybnfStructs = new LinkedList<>();
	}

	private void includes() {
		if (!ybnfStructs.isEmpty()) {
			return;
		}
		try {
			buildIncludes(new HashSet<>());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void buildIncludes(Set<Object> sets) throws Exception {
		for (Object include : includes) {
			if (sets.contains(include)) {
				continue;
			}
			sets.add(include);
			YbnfStruct ybnfStruct = null;
			if (include.equals("original.ybnf")) {
				ybnfStruct = new YbnfStruct();
				ybnfStruct.setVersion(getVersion());
				ybnfStruct.setCharset(getCharset());
				ybnfStruct.setBody(new OriginalInclude().readContent());
			} else {
				ybnfStruct = JCompiler.convertGrammar((String) include);
				ybnfStruct.buildIncludes(sets);
			}
			ybnfStructs.add(ybnfStruct);
		}
	}

	private String runCallables(String text) {
		if (semanticCallable == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (CallableStruct callableStruct : callableStructs) {
			String semantic = "'null'";
			if (text != null && !"".equals(text)) {
				semantic = semanticCallable.call(text, callableStruct.getCallName(),
						callableStruct.getArgs().toArray());
			}
			sb.append("$_call_").append(callableStruct.getCallName());
			for (Object arg : callableStruct.getArgs()) {
				sb.append("_").append(arg);
			}
			sb.append("_ = ").append(semantic).append(";\n");
		}
		for (YbnfStruct ybnfStruct : ybnfStructs) {
			ybnfStruct.setSemanticCallable(semanticCallable);
			sb.append(ybnfStruct.runCallables(text).trim());
		}
		return sb.toString().trim();
	}

	private String buildCallTempVars() {
		StringBuilder sb = new StringBuilder();
		for (CallableStruct callableStruct : callableStructs) {
			sb.append("<_call_").append(callableStruct.getCallName());
			for (Object arg : callableStruct.getArgs()) {
				sb.append("_").append(arg);
			}
			sb.append("_> = 'null'\n");
		}
		for (YbnfStruct ybnfStruct : ybnfStructs) {
			sb.append(ybnfStruct.buildCallTempVars());
		}
		return sb.toString();
	}

	private String mergeGrammar() {
		StringBuilder sb = new StringBuilder();
		if (main != null) {
			sb.append(main).append("\n");
		}
		if (body != null) {
			sb.append(body).append("\n");
		}
		for (YbnfStruct ybnfStruct : ybnfStructs) {
			sb.append(ybnfStruct.mergeGrammar().trim()).append("\n");
		}
		return sb.toString().trim();
	}

	public synchronized void initGrammarSchema() {
		if (grammarSchema == null) {
			includes();
			String grammar = mergeGrammar() + "\n" + buildCallTempVars();
			grammarSchema = CompilerUtils.parser(grammar);
			try {
				genSchema("");
			} catch (Exception e) {
			}
		}
	}

	public Object genSchema(String lang) throws Exception {
		if (grammarSchema == null) {
			initGrammarSchema();
		}
		Object schema = grammarSchema;
		String callBuffer = runCallables(lang);
		if (!callBuffer.isEmpty()) {
			StringBuilder sb = new StringBuilder("#YBNF ");
			sb.append(getVersion()).append(" ").append(getCharset()).append(";\n").append(callBuffer);
			YbnfStruct ybnfStruct = JCompiler.convertGrammar(sb.toString());
			schema = mergeSchema((Parser) grammarSchema, (Parser) ybnfStruct.genSchema(lang));
		}
		return schema;
	}

	private Object mergeSchema(Parser parent, Parser child) {
		Object sch = CompilerUtils.into(parent.grammar, child.grammar);
		Object hashMap = CompilerUtils.hashmap(CompilerUtils.keyword("grammar"), sch,
				CompilerUtils.keyword("start-production"), CompilerUtils.keyword("root"),
				CompilerUtils.keyword("output-format"), CompilerUtils.keyword("hiccup"));
		return Parser.create((IPersistentMap) hashMap);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Set<Object> getIncludes() {
		return includes;
	}

	public List<CallableStruct> getCallables() {
		return callableStructs;
	}

	public Map<String, VarnameStruct> getKvs() {
		Map<String, VarnameStruct> map = new HashMap<>();
		if (kvs != null) {
			map.putAll(kvs);
		}
		for (YbnfStruct ybnfStruct : ybnfStructs) {
			if (ybnfStruct.getKvs() != null) {
				map.putAll(ybnfStruct.getKvs());
			}
		}
		return map;
	}

	public void setKvs(Map<String, VarnameStruct> kvs) {
		this.kvs = kvs;
	}

	public void setSemanticCallable(SemanticCallable semanticCallable) {
		this.semanticCallable = semanticCallable;
	}
}
