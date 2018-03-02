package com.ybnf.compiler.lucene;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public abstract class LuceneService implements AutoCloseable {
	protected static Directory directory = new RAMDirectory();
	
	public abstract void close() throws Exception;
}
