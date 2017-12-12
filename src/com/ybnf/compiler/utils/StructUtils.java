package com.ybnf.compiler.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StructUtils {
	public static abstract class Base<T> {
		private T type;
		private Object value;

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public T getType() {
			return type;
		}

		public void setType(T type) {
			this.type = type;
		}
	}

	public enum StructType {
		Header {
			@Override
			public void parse(YbnfStruct ybnfStruct, Object value) {
				HeaderStruct headerStruct = (HeaderStruct) value;
				ybnfStruct.setVersion(headerStruct.getVersion());
				ybnfStruct.setCharset(headerStruct.getCharset());
				ybnfStruct.setService(headerStruct.getService());
				ybnfStruct.getIncludes().addAll(headerStruct.getIncludes());
			}
		},
		Callable {
			@SuppressWarnings("unchecked")
			@Override
			public void parse(YbnfStruct ybnfStruct, Object value) {
				ybnfStruct.getCallables().addAll((List<CallableStruct>) value);
			}
		},
		Main {
			@Override
			public void parse(YbnfStruct ybnfStruct, Object value) {
				ybnfStruct.setMain((String) value);
			}
		},
		Body {
			@Override
			public void parse(YbnfStruct ybnfStruct, Object value) {
				BodyStruct bs = (BodyStruct) value;
				ybnfStruct.setBody(bs.getBody());
				ybnfStruct.setKvs(bs.getKvs());
			}
		};

		public abstract void parse(YbnfStruct ybnfStruct, Object value);
	}

	public static class Struct extends Base<StructType> {
		public Struct(Object value, StructType type) {
			this.setValue(value);
			this.setType(type);
		}
	}

	public enum HeaderType {
		Version {
			@Override
			public void convert(HeaderStruct head, Object value) {
				head.setVersion((String) value);
			}
		},
		Charset {
			@Override
			public void convert(HeaderStruct head, Object value) {
				head.setCharset((String) value);
			}
		},
		Service {
			@Override
			public void convert(HeaderStruct head, Object value) {
				head.setService((String) value);
			}
		},
		Include {
			@Override
			public void convert(HeaderStruct head, Object value) {
				head.getIncludes().add(value);
			}
		};

		public abstract void convert(HeaderStruct head, Object value);
	}

	public static class Header extends Base<HeaderType> {
		public Header(Object value, HeaderType type) {
			this.setValue(value);
			this.setType(type);
		}
	}

	public static class HeaderStruct {
		private String version;
		private String charset;
		private String service;
		private List<Object> includes;

		public HeaderStruct() {
			includes = new LinkedList<>();
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public List<Object> getIncludes() {
			return includes;
		}
	}

	public static class CallableStruct {
		private String callName;
		private List<Object> args;

		public CallableStruct(String callName) {
			setCallName(callName);
			args = new LinkedList<>();
		}

		public String getCallName() {
			return callName;
		}

		public void setCallName(String callName) {
			this.callName = callName;
		}

		public List<Object> getArgs() {
			return args;
		}
	}

	public static class VarnameStruct {
		private String name;
		private String key;
		private String value;

		public VarnameStruct(String name, String key, String value) {
			this.name = name;
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "$" + name + "{" + key + "%" + value + "}";
		}

		public String getName() {
			return name;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
	}

	public static class DefineStruct {
		private VarnameStruct varnameStruct;
		private String sentence;

		public DefineStruct(VarnameStruct varnameStruct, String sentence) {
			this.varnameStruct = varnameStruct;
			this.sentence = sentence;
		}

		public VarnameStruct getVarnameStruct() {
			return varnameStruct;
		}

		public String getSentence() {
			return sentence;
		}
	}

	public static class BodyStruct {
		private String body;
		private Map<String, VarnameStruct> kvs;

		public BodyStruct() {
			kvs = new HashMap<>();
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public Map<String, VarnameStruct> getKvs() {
			return kvs;
		}
	}
}
