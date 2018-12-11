package com.ybnf.compiler.lucene;

import java.nio.file.Paths;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public abstract class LuceneService implements AutoCloseable {
	protected static Directory directory = null;

	public LuceneService(String path) throws Exception {
		if (directory == null) {
			directory = FSDirectory.open(Paths.get(path));
		}
	}

	public abstract void close() throws Exception;
}
