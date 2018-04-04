package com.ybnf.compiler.utils;

import java.util.LinkedList;
import java.util.List;

import com.ybnf.compiler.utils.StructUtils.BodyStruct;
import com.ybnf.compiler.utils.StructUtils.CallableStruct;
import com.ybnf.compiler.utils.StructUtils.DefineStruct;
import com.ybnf.compiler.utils.StructUtils.Header;
import com.ybnf.compiler.utils.StructUtils.HeaderStruct;
import com.ybnf.compiler.utils.StructUtils.HeaderType;
import com.ybnf.compiler.utils.StructUtils.Struct;
import com.ybnf.compiler.utils.StructUtils.StructType;
import com.ybnf.compiler.utils.StructUtils.VarnameStruct;

import clojure.lang.AFn;
import clojure.lang.ISeq;
import clojure.lang.RT;

public class ParserUtils {

	public final static Object[] PARSER_FUNCS = { CompilerUtils.keyword("root"), new Root(),
			CompilerUtils.keyword("head"), new Head(), CompilerUtils.keyword("callable"), new Callable(),
			CompilerUtils.keyword("call"), new Call(), CompilerUtils.keyword("include"), new Include(),
			CompilerUtils.keyword("main"), new Main(), CompilerUtils.keyword("text"), new Text(),
			CompilerUtils.keyword("variable"), new Variable(), CompilerUtils.keyword("varname"), new Varname(),
			CompilerUtils.keyword("key"), new Key(), CompilerUtils.keyword("value"), new Value(),
			CompilerUtils.keyword("choices"), new Choices(), CompilerUtils.keyword("group"), new Group(),
			CompilerUtils.keyword("zeroMore"), new ZeroMore(), CompilerUtils.keyword("ranges"), new Ranges(),
			CompilerUtils.keyword("sentence"), new Sentence(), CompilerUtils.keyword("define"), new Define(),
			CompilerUtils.keyword("body"), new Body(), CompilerUtils.keyword("orr"), new Orr(),
			CompilerUtils.keyword("filename"), new Filename(), CompilerUtils.keyword("version"), new Version(),
			CompilerUtils.keyword("charset"), new Charset(), CompilerUtils.keyword("service"), new Service() };

	private static class Root extends AFn {
		@SuppressWarnings("unchecked")
		@Override
		public Object applyTo(ISeq arglist) {
			YbnfStruct ybnfStruct = new YbnfStruct();
			for (Struct struct : (Iterable<Struct>) arglist) {
				if (struct != null) {
					struct.getType().parse(ybnfStruct, struct.getValue());
				}
			}
			return ybnfStruct;
		}
	}

	private static class Head extends AFn {
		@SuppressWarnings("unchecked")
		@Override
		public Object applyTo(ISeq arglist) {
			HeaderStruct headerStruct = new HeaderStruct();
			for (Header header : (Iterable<Header>) arglist) {
				header.getType().convert(headerStruct, header.getValue());
			}
			return new Struct(headerStruct, StructType.Header);
		}
	}

	private static class Callable extends AFn {
		@SuppressWarnings("unchecked")
		@Override
		public Object applyTo(ISeq arglist) {
			List<CallableStruct> callableStructs = new LinkedList<>();
			for (CallableStruct arg : (Iterable<CallableStruct>) arglist) {
				callableStructs.add(arg);
			}
			return new Struct(callableStructs, StructType.Callable);
		}
	}

	private static class Call extends AFn {
		@SuppressWarnings("unchecked")
		@Override
		public Object applyTo(ISeq arglist) {
			CallableStruct callableStruct = new CallableStruct((String) arglist.first());
			ISeq args = arglist.next();
			if (args != null) {
				for (Object arg : (Iterable<Object>) args) {
					callableStruct.getArgs().add(arg);
				}
			}
			return callableStruct;
		}
	}

	private static class Main extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return new Struct("root = " + arglist.first(), StructType.Main);
		}
	}

	private static class Text extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return "'" + arglist.first() + "'";
		}
	}

	private static class Variable extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return arglist.first();
		}
	}

	private static class Key extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return arglist.first();
		}
	}

	private static class Value extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return arglist.first();
		}
	}

	private static class Varname extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			String name = null, key = null, value = null;
			switch (arglist.count()) {
			case 1:
				name = "<" + arglist.first() + ">";
				break;
			case 2:
				name = (String) arglist.first();
				key = (String) arglist.next().first();
				break;
			case 3:
				name = (String) arglist.first();
				key = (String) arglist.next().first();
				value = (String) arglist.next().next().first();
				break;
			}
			return new VarnameStruct(name, key, value);
		}
	}

	private static class Choices extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return "[" + arglist.first() + "]";
		}
	}

	private static class Group extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return "(" + arglist.first() + ")";
		}
	}

	private static class ZeroMore extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return "{" + arglist.first() + "}";
		}
	}

	private static class Ranges extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			Object varname = arglist.first();
			int start = Integer.parseInt((String) RT.second(arglist));
			Object end = RT.third(arglist);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < start; i++) {
				sb.append(varname).append(" ");
			}
			if (end != null) {
				for (int i = 0; i < Integer.parseInt((String) end) - start; i++) {
					sb.append("[").append(varname).append("] ");
				}
			}
			return sb.toString();
		}
	}

	private static class Sentence extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return CompilerUtils.join(arglist, " ");
		}
	}

	private static class Define extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return new DefineStruct((VarnameStruct) arglist.first(), (String) arglist.next().first());
		}
	}

	private static class Body extends AFn {
		@SuppressWarnings("unchecked")
		@Override
		public Object applyTo(ISeq arglist) {
			if (arglist == null) {
				return null;
			}
			BodyStruct bs = new BodyStruct();
			StringBuilder sb = new StringBuilder();
			for (DefineStruct defineStruct : (Iterable<DefineStruct>) arglist) {
				VarnameStruct vs = defineStruct.getVarnameStruct();
				sb.append(vs.getName()).append(" = ").append(defineStruct.getSentence()).append("\n");
				if (vs.getKey() != null) {
					bs.getKvs().put(vs.getName(), vs);
				}
			}
			bs.setBody(sb.toString().trim());
			return new Struct(bs, StructType.Body);
		}
	}

	private static class Orr extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return CompilerUtils.join(arglist, "|");
		}
	}

	private static class Include extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return new Header(arglist.first(), HeaderType.Include);
		}
	}

	private static class Filename extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			String filename = (String) arglist.first();
			if (filename.startsWith("classpath:")) {
				String[] filenames = filename.split(":", 2);
				return CompilerUtils.getResourcePath(filenames[1]);
			} else if (filename.startsWith("file:")) {
				String[] filenames = filename.split(":", 2);
				return filenames[1];
			}
			return filename;
		}
	}

	private static class Version extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return new Header(arglist.first(), HeaderType.Version);
		}
	}

	private static class Charset extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return new Header(arglist.first(), HeaderType.Charset);
		}
	}

	private static class Service extends AFn {
		@Override
		public Object applyTo(ISeq arglist) {
			return new Header(arglist.first(), HeaderType.Service);
		}
	}
}
