package com.ybnf.compiler.impl;

import java.util.HashMap;
import java.util.Map;

import com.ybnf.compiler.MLCompiler;

import edu.mit.ll.mitie.EntityMention;
import edu.mit.ll.mitie.EntityMentionVector;
import edu.mit.ll.mitie.NamedEntityExtractor;
import edu.mit.ll.mitie.SDPair;
import edu.mit.ll.mitie.StringVector;
import edu.mit.ll.mitie.TextCategorizer;
import edu.mit.ll.mitie.TotalWordFeatureExtractor;

public class MITIECompiler extends MLCompiler {
	private static Map<String, TextCategorizer> serviceCategorizers = new HashMap<>();
	private static Map<String, TextCategorizer> intentCategorizers = new HashMap<>();
	private static Map<String, NamedEntityExtractor> namedEntityExtractors = new HashMap<>();
	private static TotalWordFeatureExtractor totalWordFeatureExtractor = null; // 公用语言模型（MITIE lib yyd自定义）
	private TextCategorizer serviceCategorizer = null;
	private TextCategorizer intentCategorizer = null;
	private NamedEntityExtractor namedEntityExtractor = null;
	private StringVector possibleTags;

	public MITIECompiler(String serviceCategoryFilename, String featureExtractorFilename) {
		this(serviceCategoryFilename, null, null, featureExtractorFilename);
	}

	public MITIECompiler(String serviceCategoryFilename, String intentCategoryFilename, String namedEntityFilename,
			String featureExtractorFilename) {
		// 公用语言模型初始化
		if (totalWordFeatureExtractor == null) {
			totalWordFeatureExtractor = new TotalWordFeatureExtractor(featureExtractorFilename);
		}
		// 场景分类模型初始化
		serviceCategorizer = serviceCategorizers.get(serviceCategoryFilename);
		if (serviceCategorizer == null) {
			serviceCategorizer = new TextCategorizer(serviceCategoryFilename);
			serviceCategorizers.put(serviceCategoryFilename, serviceCategorizer);
		}
		// 意图分类模型初始化
		if (intentCategoryFilename != null) {
			intentCategorizer = intentCategorizers.get(intentCategoryFilename);
			if (intentCategorizer == null) {
				intentCategorizer = new TextCategorizer(intentCategoryFilename);
				intentCategorizers.put(intentCategoryFilename, intentCategorizer);
			}
		}
		// 命名实体识别模型初始化
		if (namedEntityFilename != null) {
			namedEntityExtractor = namedEntityExtractors.get(namedEntityFilename);
			if (namedEntityExtractor == null) {
				namedEntityExtractor = new NamedEntityExtractor(namedEntityFilename);
				namedEntityExtractors.put(namedEntityFilename, namedEntityExtractor);
			}
			possibleTags = namedEntityExtractor.getPossibleNerTags();
		}
	}

	@Override
	protected String service(String lang) throws Exception {
		StringVector sv = new StringVector();
		for (int i = 0; i < lang.length(); i++) {
			sv.add(lang.substring(i, i + 1));
		}
		SDPair pair = serviceCategorizer.categorizeDoc(sv, totalWordFeatureExtractor);
		String service = pair.getFirst();
		if (0.3d > pair.getSecond()) {
			throw new Exception("MITIE Service match fail! (" + service + "'s score is less than 0.3)");
		}
		return pair.getFirst();
	}

	@Override
	protected String intent(String lang) throws Exception {
		if (intentCategorizer == null) {
			return null;
		}
		StringVector sv = new StringVector();
		for (int i = 0; i < lang.length(); i++) {
			sv.add(lang.substring(i, i + 1));
		}
		SDPair pair = intentCategorizer.categorizeDoc(sv, totalWordFeatureExtractor);
		String service = pair.getFirst();
		if (0.3d > pair.getSecond()) {
			throw new Exception("MITIE Intent match fail! (" + service + "'s score is less than 0.3)");
		}
		return pair.getFirst();
	}

	@Override
	protected Map<String, String> entities(String lang) throws Exception {
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
}
