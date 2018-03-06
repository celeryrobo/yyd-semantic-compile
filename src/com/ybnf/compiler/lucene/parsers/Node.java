package com.ybnf.compiler.lucene.parsers;

import com.ybnf.compiler.lucene.TemplateBuilder;

public abstract class Node<T> {
	protected T data;

	public abstract TemplateBuilder build();
	
	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		Node<T> node = (Node<T>) obj;
		return data.equals(node.data);
	}
}
