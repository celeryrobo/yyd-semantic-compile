package com.ybnf.compiler.lucene;

import java.util.Collection;
import java.util.Map;

import org.ansj.library.DicLibrary;
import org.ansj.lucene7.AnsjAnalyzer;
import org.ansj.lucene7.AnsjAnalyzer.TYPE;
import org.ansj.util.MyStaticValue;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.StringUtil;

public class IndexWriterService extends LuceneService {
	private IndexWriter writer;

	public IndexWriterService(Collection<String> dics) throws Exception {
		for (String dic : dics) {
			if (!MyStaticValue.ENV.containsKey(dic)) {
				DicLibrary.put(dic, "", new Forest());
			}
		}
		Analyzer analyzer = new AnsjAnalyzer(TYPE.dic_ansj, StringUtil.joiner(dics, ","));
		IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(directory, cfg);
	}

	public void addTemplateEntity(TemplateEntity entity) throws Exception {
		Document doc = new Document();
		doc.add(new StringField("id", entity.getId().toString(), Store.YES));
		doc.add(new StringField("appId", entity.getAppId().toString(), Store.YES));
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
				addTemplateEntity(new TemplateEntity(0, 0, serviceName, intentName, template.getTemplate()));
			}
			entry.getValue().resetTemplates();
		}
	}

	public long deleteMany(Query... queries) throws Exception {
		return writer.deleteDocuments(queries);
	}

	public long deleteAll() throws Exception {
		return writer.deleteAll();
	}

	@Override
	public void close() throws Exception {
		writer.commit();
		writer.close();
	}
}
