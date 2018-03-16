package com.ybnf.compiler.lucene;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class IndexReaderService extends LuceneService {
	private static final Logger LOG = Logger.getLogger(IndexReaderService.class.getSimpleName());
	private IndexReader reader;
	private IndexSearcher searcher;

	public IndexReaderService() throws Exception {
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
	}

	public List<TemplateEntity> search(Query query) throws Exception {
		TopDocs docs = searcher.search(query, 10);
		LOG.info("total hits : " + docs.totalHits);
		if (docs.totalHits == 0) {
			return null;
		}
		List<TemplateEntity> entities = new LinkedList<>();
		for (ScoreDoc scoreDoc : docs.scoreDocs) {
			entities.add(buildTemplateEntity(scoreDoc));
		}
		return entities;
	}

	private TemplateEntity buildTemplateEntity(ScoreDoc scoreDoc) throws Exception {
		Document doc = searcher.doc(scoreDoc.doc);
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
