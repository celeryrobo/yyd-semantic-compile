package com.ybnf.compiler.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public abstract class LuceneService implements AutoCloseable {
	protected static Directory directory;
	static {
		try {
			String platform = System.getProperty("os.name", "linux");
			String path = "/tmp/lucene-indexes";
			if (platform.toLowerCase().startsWith("win")) {
				path = "E:" + path;
			}
			directory = FSDirectory.open(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public abstract void close() throws Exception;
}
