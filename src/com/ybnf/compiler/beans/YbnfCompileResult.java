package com.ybnf.compiler.beans;

import java.util.Map;

public class YbnfCompileResult {
	private String text;
	private String version;
	private String charset;
	private String service;
	private Map<String, String> objects;
	private Map<String, String> slots;

	public YbnfCompileResult(String text, String version, String charset, String service, Map<String, String> objects,
			Map<String, String> slots) {
		this.text = text;
		this.version = version;
		this.charset = charset;
		this.service = service;
		this.objects = objects;
		this.slots = slots;
	}

	public String getText() {
		return text;
	}

	public String getVersion() {
		return version;
	}

	public String getCharset() {
		return charset;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Map<String, String> getObjects() {
		return objects;
	}

	public Map<String, String> getSlots() {
		return slots;
	}
	
	public void setSlots(Map<String, String> slots) {
		this.slots = slots;
	}

	@Override
	public String toString() {
		return "YbnfCompileResult [text=" + text + ", version=" + version + ", charset=" + charset + ", service="
				+ service + ", objects=" + objects + ", slots=" + slots + "]";
	}

	

}
