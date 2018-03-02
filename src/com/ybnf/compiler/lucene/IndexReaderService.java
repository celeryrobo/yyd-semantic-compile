package com.ybnf.compiler.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class IndexReaderService extends LuceneService {
	private IndexReader reader;
	private IndexSearcher searcher;

	public IndexReaderService() throws Exception {
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
	}
	
	public TopDocs search(Query query, int n) throws Exception {
		return searcher.search(query, n);
	}
	
	public TemplateEntity getTemplateEntity(ScoreDoc scoreDoc) throws Exception {
		Document doc =  searcher.doc(scoreDoc.doc);
		String service = doc.get("service");
		String intent = doc.get("intent");
		String template = doc.get("template");
		return new TemplateEntity(service, intent, template);
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}
}
