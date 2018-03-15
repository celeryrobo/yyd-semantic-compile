package com.ybnf.compiler.lucene;

import java.util.Map;

import org.ansj.lucene7.AnsjAnalyzer;
import org.ansj.lucene7.AnsjAnalyzer.TYPE;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

public class IndexWriterService extends LuceneService {
	private static Analyzer analyzer = new AnsjAnalyzer(TYPE.dic_ansj);
	private IndexWriter writer;

	public IndexWriterService() throws Exception {
		IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(directory, cfg);
	}

	public void addTemplateEntity(TemplateEntity entity) throws Exception {
		Document doc = new Document();
		doc.add(new StringField("service", entity.getService(), Store.YES));
		doc.add(new StringField("intent", entity.getIntent(), Store.YES));
		doc.add(new TextField("template", entity.getTemplate(), Store.YES));
		writer.addDocument(doc);
	}

	public void initSemanticService(SemanticService service) throws Exception {
		String serviceName = service.getName();
		Map<String, SemanticIntent> intents = service.getIntents();
		for (Map.Entry<String, SemanticIntent> entry : intents.entrySet()) {
			String intentName = entry.getKey();
			for (Template template : entry.getValue().getTemplates()) {
				addTemplateEntity(new TemplateEntity(serviceName, intentName, template.getTemplate()));
			}
			entry.getValue().resetTemplates();
		}
	}

	@Override
	public void close() throws Exception {
		writer.commit();
		writer.close();
	}
}
