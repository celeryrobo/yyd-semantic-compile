package com.ybnf.compiler;

import java.util.HashMap;
import java.util.Map;
import com.ybnf.compiler.beans.YbnfCompileResult;

public abstract class MLCompiler implements ICompiler {
	@Override
	public YbnfCompileResult compile(String lang) throws Exception {
		String service = service(lang);
		if (service == null) {
			throw new Exception("MITIE Service and Intent is error!");
		}
		String intent = intent(lang);
		Map<String, String> slots = new HashMap<>();
		if (intent != null) {
			slots.put("intent", intent);
		}
		return new YbnfCompileResult(lang, "0.1", "UTF-8", service, entities(lang), slots);
	}

	protected abstract String service(String lang) throws Exception;

	protected abstract String intent(String lang) throws Exception;

	protected abstract Map<String, String> entities(String lang) throws Exception;
}
