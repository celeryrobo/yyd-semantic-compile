package com.ybnf.compiler.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import instaparse.gll.Failure;

public class CompilerUtils {
	private final static String CLOJURE_CORE = "clojure.core";
	private final static IFn REQUIRE = RT.var(CLOJURE_CORE, "require");
	private final static IFn KEYWORD = RT.var(CLOJURE_CORE, "keyword");
	private final static IFn INTO = RT.var(CLOJURE_CORE, "into");
	private final static IFn NEXT = RT.var(CLOJURE_CORE, "next");
	private final static IFn FIRST = RT.var(CLOJURE_CORE, "first");
	private final static IFn COUNT = RT.var(CLOJURE_CORE, "count");
	private final static IFn HASHMAP = RT.var(CLOJURE_CORE, "hash-map");

	private final static String CLOJURE_PPRINT = "clojure.pprint";
	private final static IFn PPRINT = RT.var(CLOJURE_PPRINT, "pprint");

	private final static String INSTAPARSE_CORE = "instaparse.core";
	private final static IFn PARSER = RT.var(INSTAPARSE_CORE, "parser");
	private final static IFn PARSE = RT.var(INSTAPARSE_CORE, "parse");
	private final static IFn TRANSFORM = RT.var(INSTAPARSE_CORE, "transform");
	private final static IFn GET_FAILURE = RT.var(INSTAPARSE_CORE, "get-failure");
	private final static IFn IS_FAILURE = RT.var(INSTAPARSE_CORE, "failure?");

	static {
		require("instaparse.core");
		require("clojure.pprint");
	}

	public static Object require(String ns) {
		return REQUIRE.invoke(RT.readString(ns));
	}

	public static Keyword keyword(String name) {
		return (Keyword) KEYWORD.invoke(name);
	}

	public static Object hashmap(Object... kw) {
		return HASHMAP.applyTo(RT.arrayToList(kw));
	}

	public static Object into(Object first, Object second) {
		return INTO.invoke(first, second);
	}

	public static Object next(Object seq) {
		return NEXT.invoke(seq);
	}

	public static Object first(Object seq) {
		return FIRST.invoke(seq);
	}

	public static Object count(Object seq) {
		return COUNT.invoke(seq);
	}

	public static IPersistentMap map(Object... objs) {
		return RT.map(objs);
	}

	public static Object mapGet(Object map, String name) {
		return mapGet(map, keyword(name));
	}

	public static Object mapGet(Object map, Keyword kw) {
		return kw.invoke(map);
	}

	public static void pprint(Object o) {
		PPRINT.invoke(o);
	}

	public static Object parser(String grammar) {
		return PARSER.invoke(grammar);
	}

	public static Object parse(Object schema, String lang) {
		return PARSE.invoke(schema, lang);
	}

	public static Object transform(Object fnMap, Object tree) {
		return TRANSFORM.invoke(fnMap, tree);
	}

	public static Boolean isFailure(Object tree) {
		return (Boolean) IS_FAILURE.invoke(tree);
	}

	public static Failure getFailure(Object tree) {
		return (Failure) GET_FAILURE.invoke(tree);
	}

	@SuppressWarnings("unchecked")
	public static String toFailure(Failure failure) {
		StringBuilder sb = new StringBuilder();
		sb.append("index    : ").append(failure.index);
		sb.append("\nline     : ").append(mapGet(failure, "line"));
		sb.append("\ncolumn   : ").append(mapGet(failure, "column"));
		sb.append("\ntext     : ").append(mapGet(failure, "text"));
		sb.append("\nreason   : ");
		for (Object reason : (Iterable<Object>) failure.reason) {
			sb.append(mapGet(reason, "expecting")).append("; ");
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public static String join(ISeq args, String sep) {
		if (args.count() < 1) {
			return "";
		}
		boolean needSep = false;
		StringBuilder sb = new StringBuilder();
		for (Object arg : (Iterable<Object>) args) {
			if (needSep) {
				sb.append(sep);
			} else {
				needSep = true;
			}
			sb.append(arg);
		}
		return sb.toString();
	}

	public static String getResourcePath(String name) {
		return CompilerUtils.class.getResource("/").getPath() + name;
	}

	public static String readFile(String filename) throws Exception {
		return readFile(new File(filename));
	}

	public static String readFile(File file) throws Exception {
		return readFile(fileReader(file));
	}

	public static String readFile(InputStream inputStream) throws Exception {
		return readFile(fileReader(inputStream));
	}

	public static String readFile(BufferedReader bufferedReader) throws Exception {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		bufferedReader.close();
		return sb.toString();
	}

	public static BufferedReader fileReader(InputStream inputStream) throws Exception {
		InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
		return new BufferedReader(isr);
	}

	public static BufferedReader fileReader(File file) throws Exception {
		BufferedReader br = null;
		if (file.exists()) {
			br = fileReader(new FileInputStream(file));
		}
		return br;
	}

	public static BufferedReader fileReader(String filename) throws Exception {
		return fileReader(new File(filename));
	}
}
