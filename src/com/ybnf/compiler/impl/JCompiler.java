package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.compiler.utils.CompilerUtils;
import com.ybnf.compiler.utils.ParserUtils;
import com.ybnf.compiler.utils.StructUtils.VarnameStruct;
import com.ybnf.semantic.SemanticCallable;
import com.ybnf.compiler.utils.YbnfStruct;

import clojure.lang.Keyword;
import clojure.lang.Sequential;
import ybnf.jutils;

class Engine {
	private static Object schema = null;

	public Engine() {
		if (schema == null) {
			try {
				schema = CompilerUtils.parser(jutils.getGrammar());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public Object compile(String grammar) throws Exception {
		Object tree = CompilerUtils.parse(schema, grammar);
		if (CompilerUtils.isFailure(tree)) {
			throw new Exception(CompilerUtils.toFailure(CompilerUtils.getFailure(tree)));
		}
		return CompilerUtils.transform(CompilerUtils.map(ParserUtils.PARSER_FUNCS), tree);
	}
}

public class JCompiler implements ICompiler {
	public final static Engine ENGINE = new Engine();
	private YbnfStruct ybnfStruct;

	public JCompiler(String grammar) throws Exception {
		ybnfStruct = convertGrammar(grammar);
		ybnfStruct.initGrammarSchema();
	}

	public static YbnfStruct convertGrammar(String grammar) throws Exception {
		Object trans = null;
		if (grammar.endsWith(".ybnf")) {
			trans = ENGINE.compile(CompilerUtils.readFile(grammar));
		} else {
			trans = ENGINE.compile(grammar);
		}
		return (YbnfStruct) trans;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> buildKeyword(Object sequential, Map<String, VarnameStruct> kvs) {
		Map<String, Object> params = new HashMap<>();
		Keyword key = (Keyword) CompilerUtils.first(sequential);
		Object nextSent = CompilerUtils.next(sequential);
		for (Object o : (Iterable<Object>) nextSent) {
			if (o instanceof Sequential) {
				params.putAll(buildKeyword(o, kvs));
			}
		}
		if (kvs.containsKey(key.getName())) {
			params.put(key.getName(), sequential);
		}
		return params;
	}

	@SuppressWarnings("unchecked")
	private static String buildSentence(Object sequential) {
		StringBuilder sb = new StringBuilder();
		Object nextSent = CompilerUtils.next(sequential);
		for (Object o : (Iterable<Object>) nextSent) {
			if (o instanceof Sequential) {
				sb.append(buildSentence(o));
			} else {
				sb.append(o);
			}
		}
		return sb.toString();
	}

	public YbnfCompileResult execCompile(String lang) throws Exception {
		Object schema = ybnfStruct.genSchema(lang);
		Object tree = CompilerUtils.parse(schema, lang);
		if (CompilerUtils.isFailure(tree)) {
			throw new Exception(CompilerUtils.toFailure(CompilerUtils.getFailure(tree)));
		}
		Map<String, VarnameStruct> kvs = ybnfStruct.getKvs();
		Map<String, Object> rs = buildKeyword(tree, kvs);
		Map<String, String> objects = new HashMap<>();
		Map<String, String> slots = new HashMap<>();
		for (Entry<String, Object> entry : rs.entrySet()) {
			VarnameStruct vs = kvs.get(entry.getKey());
			if (vs.getValue() == null) {
				objects.put(vs.getKey(), buildSentence(entry.getValue()));
			} else {
				slots.put(vs.getKey(), vs.getValue());
			}
		}
		return new YbnfCompileResult(lang, ybnfStruct.getVersion(), ybnfStruct.getCharset(), ybnfStruct.getService(),
				objects, slots);
	}

	@Override
	public void setSemanticCallable(SemanticCallable semanticCallable) {
		ybnfStruct.setSemanticCallable(semanticCallable);
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		return execCompile(text);
	}
}
