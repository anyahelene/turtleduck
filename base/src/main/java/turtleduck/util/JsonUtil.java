package turtleduck.util;

import java.util.stream.Collectors;

import turtleduck.colors.Color;

public class JsonUtil {

	public static Object decodeJson(String s) {
		throw new UnsupportedOperationException();
	}

	public static <T> T decodeJson(String s, Class<T> type) {
		if (type == String.class) {
			if (s.startsWith("\"") || s.startsWith("'"))
				throw new UnsupportedOperationException();
			else
				return (T) s;
		} else if (type == Boolean.class) {
			return (T) Boolean.valueOf(s);
		} else if (type == Double.class) {
			return (T) Double.valueOf(s);
		} else if (type == Float.class) {
			return (T) Float.valueOf(s);
		} else if (type == Integer.class) {
			return (T) Integer.valueOf(s);
		} else if (type == Long.class) {
			return (T) Long.valueOf(s);
		} else if (type == Short.class) {
			return (T) Short.valueOf(s);
		} else if (type == Byte.class) {
			return (T) Byte.valueOf(s);
		} else if (type == Color.class && s.startsWith("#")) {
			return (T) Color.fromString(s);
		} else {
			throw new IllegalArgumentException("Can't decode " + type.getName());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T decodeValue(String s, Class<T> type) {
		if (type == String.class) {
			return (T) s;
		} else if (type == Boolean.class) {
			return (T) Boolean.valueOf(s);
		} else if (type == Double.class) {
			return (T) Double.valueOf(s);
		} else if (type == Float.class) {
			return (T) Float.valueOf(s);
		} else if (type == Integer.class) {
			return (T) Integer.valueOf(s);
		} else if (type == Long.class) {
			return (T) Long.valueOf(s);
		} else if (type == Short.class) {
			return (T) Short.valueOf(s);
		} else if (type == Byte.class) {
			return (T) Byte.valueOf(s);
		} else if (type == Color.class && s.startsWith("#")) {
			return (T) Color.fromString(s);
		} else {
			throw new IllegalArgumentException("Can't decode " + type.getName());
		}
	}

	public static String encodeValue(Object o) {
		if (o instanceof String) {
			return "\"" + Strings.jsonEscape(o.toString()) + "\"";
		} else if (o instanceof Number || o instanceof Boolean) {
			return o.toString();
		} else if (o instanceof Array) {
			Array a = (Array) o;
			if (a.size() == 0) {
				return "Array.of(" + a.elementType().getName() + ".class)";
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Array.of(");
				sb.append(a.stream().map((elt) -> encodeValue(elt)).collect(Collectors.joining(",")));
				sb.append(")");
				return sb.toString();
			}
		} else if (o instanceof Dict) {
			Dict d = (Dict) o;
			StringBuilder sb = new StringBuilder();
			sb.append("Dict.create()");
			d.forEach(k -> {
				sb.append(".put(");
				sb.append(k.key());
				sb.append(",");
				sb.append(encodeValue(d.get(k)));
			});
			return sb.toString();
		} else if (o instanceof Color) {
			return String.format("#%08x", ((Color) o).toARGB());
		} else {
			throw new IllegalArgumentException("Can't encode " + o);
		}
	}

	public static String encode(boolean b) {
		return Boolean.toString(b);
	}

	public static String encode(int i) {
		return Integer.toString(i);
	}

	public static String encode(double d) {
		return Double.toString(d);
	}

	public static String encode(String s) {
		return String.format("\"%s\"", Strings.jsonEscape(s));
	}

	public static String encode(Dict d) {
		StringBuilder b = new StringBuilder();
		b.append("{");
		boolean first = true;
		for (Key<?> k : d) {
			if (!first)
				b.append(",");
			else
				first = false;
			Object val = d.get(k);
			b.append(encode(k.key()));
			b.append(":");
			b.append(encode(val));
		}
		b.append("}");
		return b.toString();
	}

	public static String encode(Array a) {
		StringBuilder b = new StringBuilder();
		b.append("[");
		boolean first = true;
		for (Object obj : a) {
			if (!first)
				b.append(",");
			else
				first = false;
			b.append(encode(obj));
		}
		b.append("]");
		return b.toString();
	}

	public static String encode(Object val) {
		if (val == null)
			return "null";
		else if (val instanceof Boolean)
			return encode((boolean) val);
		else if (val instanceof Integer)
			return encode((int) val);
		else if (val instanceof Double)
			return encode((double) val);
		else if (val instanceof String)
			return encode((String) val);
		else if (val instanceof Dict)
			return encode((Dict) val);
		else if (val instanceof Array)
			return encode((Array) val);
		else
			return encode(val.toString());
	}

}
