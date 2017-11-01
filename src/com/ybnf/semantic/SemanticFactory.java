package com.ybnf.semantic;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SemanticFactory {
	private static Map<String, Semantic<?>> semanticMap; // Semantic对象缓存
	private Properties properties;

	public SemanticFactory(String filename) throws Exception {
		this(new String[] { filename });
	}

	public SemanticFactory(String[] filenames) throws Exception {
		semanticMap = new HashMap<String, Semantic<?>>();
		properties = new Properties();
		for (String filename : filenames) {
			FileInputStream fis = new FileInputStream(filename);
			properties.load(fis);
			fis.close();
		}
	}

	public Semantic<?> build(String serviceName) throws Exception {
		Semantic<?> semantic = semanticMap.get(serviceName);
		if (semantic == null) {
			String className = properties.getProperty(serviceName);
			Class<?> clazz = Class.forName(className);
			semantic = (Semantic<?>) clazz.newInstance();
			semanticMap.put(serviceName, semantic);
		}
		return semantic;
	}
}
