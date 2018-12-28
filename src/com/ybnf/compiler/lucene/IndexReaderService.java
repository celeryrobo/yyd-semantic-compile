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

	public IndexReaderService(String path) throws Exception {
		super(path);
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
	}

	public List<TemplateEntity> search(Query query) throws Exception {
		List<TemplateEntity> entities = new LinkedList<>();
		if (query == null) {
			return entities;
		}
		TopDocs docs = searcher.search(query, 10);
		LOG.info("total hits : " + docs.totalHits);
		for (ScoreDoc scoreDoc : docs.scoreDocs) {
			entities.add(buildTemplateEntity(scoreDoc));
		}
		return entities;
	}

	private TemplateEntity buildTemplateEntity(ScoreDoc scoreDoc) throws Exception {
		Document doc = searcher.doc(scoreDoc.doc);
		Integer id = Integer.valueOf(doc.get("id"));
		String service = doc.get("service");
		String intent = doc.get("intent");
		String template = doc.get("template");
		float score = scoreDoc.score;
		return new TemplateEntity(id, service, intent, template, score);
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}
}
