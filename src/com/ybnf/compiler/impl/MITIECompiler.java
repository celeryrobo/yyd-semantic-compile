package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.Map;

import com.ybnf.compiler.ICompiler;
import com.ybnf.compiler.beans.YbnfCompileResult;
import com.ybnf.semantic.SemanticCallable;

import edu.mit.ll.mitie.EntityMention;
import edu.mit.ll.mitie.EntityMentionVector;
import edu.mit.ll.mitie.NamedEntityExtractor;
import edu.mit.ll.mitie.SDPair;
import edu.mit.ll.mitie.StringVector;
import edu.mit.ll.mitie.TextCategorizer;
import edu.mit.ll.mitie.TotalWordFeatureExtractor;

public class MITIECompiler implements ICompiler {
	private static Map<String, TextCategorizer> textCategorizers = new HashMap<>();
	private static Map<String, NamedEntityExtractor> namedEntityExtractors = new HashMap<>();
	private static TotalWordFeatureExtractor totalWordFeatureExtractor = null; // 公用语言模型（MITIE lib yyd自定义）
	private TextCategorizer textCategorizer = null;
	private NamedEntityExtractor namedEntityExtractor = null;
	private StringVector possibleTags;

	public MITIECompiler(String categoryFilename, String namedEntityFilename, String featureExtractorFilename) {
		if(totalWordFeatureExtractor == null) {
			totalWordFeatureExtractor = new TotalWordFeatureExtractor(featureExtractorFilename);
		}
		textCategorizer = textCategorizers.get(categoryFilename);
		if (textCategorizer == null) {
			textCategorizer = new TextCategorizer(categoryFilename);
			textCategorizers.put(categoryFilename, textCategorizer);
		}
		if (namedEntityFilename != null) {
			namedEntityExtractor = namedEntityExtractors.get(namedEntityFilename);
			if (namedEntityExtractor == null) {
				namedEntityExtractor = new NamedEntityExtractor(namedEntityFilename);
				namedEntityExtractors.put(namedEntityFilename, namedEntityExtractor);
			}
			possibleTags = namedEntityExtractor.getPossibleNerTags();
		}
	}

	private SDPair categorizer(String lang) {
		StringVector sv = new StringVector();
		for (int i = 0; i < lang.length(); i++) {
			sv.add(lang.substring(i, i + 1));
		}
		return textCategorizer.categorizeDoc(sv, totalWordFeatureExtractor);
	}

	private Map<String, String> namedEntityExtractor(String lang) {
		Map<String, String> result = new HashMap<>();
		if (namedEntityExtractor == null) {
			return result;
		}
		StringVector sv = new StringVector();
		for (int i = 0; i < lang.length(); i++) {
			sv.add(lang.substring(i, i + 1));
		}
		EntityMentionVector emv = namedEntityExtractor.extractEntities(sv, totalWordFeatureExtractor);
		for (int i = 0; i < emv.size(); i++) {
			EntityMention em = emv.get(i);
			String tag = possibleTags.get(em.getTag());
			double score = em.getScore();
			if (0.1d < score) {
				StringBuilder sb = new StringBuilder();
				for (int idx = em.getStart(); idx < em.getEnd(); idx++) {
					sb.append(sv.get(idx));
				}
				result.put(tag, sb.toString());
			}
		}
		return result;
	}

	@Override
	public YbnfCompileResult compile(String text) throws Exception {
		SDPair pair = categorizer(text);
		if (0.3d > pair.getSecond()) {
			return null;
		}
		String[] si = pair.getFirst().split(":", 2);
		if (si.length != 2) {
			throw new Exception("Service and Intent is error!");
		}
		String service = si[0];
		Map<String, String> slots = new HashMap<>();
		slots.put("intent", si[1]);
		Map<String, String> objects = namedEntityExtractor(text);
		return new YbnfCompileResult(text, "0.1", "UTF-8", service, objects, slots);
	}

	@Override
	public void setSemanticCallable(SemanticCallable semanticCallable) {

	}

}
