package turtleduck.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

public class Logging {
	public static final int LOG_LEVEL_TRACE = LocationAwareLogger.TRACE_INT;
	public static final int LOG_LEVEL_DEBUG = LocationAwareLogger.DEBUG_INT;
	public static final int LOG_LEVEL_INFO = LocationAwareLogger.INFO_INT;
	public static final int LOG_LEVEL_WARN = LocationAwareLogger.WARN_INT;
	public static final int LOG_LEVEL_ERROR = LocationAwareLogger.ERROR_INT;

	private static ILoggerFactory factory = null;
	private static boolean tried = false;
	private static BiConsumer<Integer, List<Object>> dest;

	public static void setFactory(ILoggerFactory factory) {
		Logging.factory = factory;
		tried = false;
	}

	public static void setLogDest(BiConsumer<Integer, List<Object>> dest) {
		Logging.dest = dest;
	}

	public static ILoggerFactory getFactory() {
		if (!tried && factory == null) {
			try {
				tried = true;
				Class<?> clazz = Logging.class.getClassLoader().loadClass("org.slf4j.LoggerFactory");
				Method method = clazz.getMethod("getILoggerFactory");
				factory = (ILoggerFactory) method.invoke(null);
			} catch (Exception e) {
			}
		}
		return factory;
	}

	public static Logger getLogger(String name) {
		getFactory();
		if (factory != null) {
			return factory.getLogger(name);
		} else {
			return new CustomLogger(name);
		}
	}

	public static Logger getLogger(Class<?> clazz) {
		getFactory();
		if (factory != null) {
			return factory.getLogger(clazz.getName());
		} else {
			return new CustomLogger(clazz.getName());
		}
	}

	public static List<Object> formatMessage(String msg, Object... args) {
		List<Object> result = new ArrayList<>();
		String DELIM = "{}";
		char ESCAPE = '\\';
		if (args == null)
			args = new Object[0];
		int len = msg.length();
		int start = 0, pos = 0;
		int i = 0;
		while (pos < len) {
			int j = msg.indexOf(DELIM, pos);
			if (j >= 0) {
				if (j > 1 && msg.charAt(j - 1) == ESCAPE) { // escaped => false alarm
					if (start < j - 1) {
						result.add(msg.subSequence(start, j - 1));
						result.add(msg.subSequence(j, j + 2));
					}
					start = pos = j + 2;
					continue;
				} else {
					if (start < j)
						result.add(msg.substring(start, j));
					start = pos = j + 2;
					if (i < args.length) {
						result.add(args[i++]);
					} else {
						result.add(null);
					}
				}
			} else {
				break;
			}
		}
		if (start < len) {
			result.add(msg.substring(start));
		}
		if (i < args.length && args[args.length - 1] instanceof Throwable) {
			result.add(args[args.length - 1]);
		}
		return result;
	}

	public static void useCustomLoggerFactory() {
		factory = (name) -> new CustomLogger(name);
	}

	static class CustomLogger implements Logger {

		private String name;
		private int level = LOG_LEVEL_INFO;

		public CustomLogger(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isTraceEnabled() {
			return level <= LOG_LEVEL_TRACE;
		}

		public String levelName(int lvl) {
			if (lvl >= LOG_LEVEL_ERROR)
				return "ERROR";
			else if (lvl >= LOG_LEVEL_WARN)
				return "WARN";
			else if (lvl >= LOG_LEVEL_INFO)
				return "INFO";
			else if (lvl >= LOG_LEVEL_DEBUG)
				return "DEBUG";
			else
				return "TRACE";
		}

		public void log(int lvl, String format) {
			log(lvl, format, new Object[] {});
		}

		public void log(int lvl, String format, Object arg) {
			log(lvl, format, new Object[] { arg });

		}

		public void log(int lvl, String format, Object arg1, Object arg2) {
			log(lvl, format, new Object[] { arg1, arg2 });
		}

		public void log(int lvl, String format, Object[] args) {
			if (level <= lvl) {
				String head = levelName(lvl) + " " + name + " - ";
				List<Object> msg = formatMessage(format, args);
				msg.add(0, head);
				if (dest != null)
					dest.accept(lvl, msg);
			}
		}

		@Override
		public void trace(String msg) {
			log(LOG_LEVEL_TRACE, msg);
		}

		@Override
		public void trace(String format, Object arg) {
			log(LOG_LEVEL_TRACE, format, arg);

		}

		@Override
		public void trace(String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_TRACE, format, arg1, arg2);

		}

		@Override
		public void trace(String format, Object... arguments) {
			log(LOG_LEVEL_TRACE, format, arguments);

		}

		@Override
		public void trace(String msg, Throwable t) {
			log(LOG_LEVEL_TRACE, msg, t);
		}

		@Override
		public boolean isTraceEnabled(Marker marker) {
			return isTraceEnabled();
		}

		@Override
		public void trace(Marker marker, String msg) {
			log(LOG_LEVEL_TRACE, msg);
		}

		@Override
		public void trace(Marker marker, String format, Object arg) {
			log(LOG_LEVEL_TRACE, format, arg);
		}

		@Override
		public void trace(Marker marker, String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_TRACE, format, arg1, arg2);

		}

		@Override
		public void trace(Marker marker, String format, Object... arguments) {
			log(LOG_LEVEL_TRACE, format, arguments);

		}

		@Override
		public void trace(Marker marker, String msg, Throwable t) {
			log(LOG_LEVEL_TRACE, msg);

		}

		@Override
		public boolean isDebugEnabled() {
			return level <= LOG_LEVEL_DEBUG;
		}

		@Override
		public void debug(String msg) {
			log(LOG_LEVEL_DEBUG, msg);

		}

		@Override
		public void debug(String format, Object arg) {
			log(LOG_LEVEL_DEBUG, format, arg);

		}

		@Override
		public void debug(String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_DEBUG, format, arg1, arg2);

		}

		@Override
		public void debug(String format, Object... arguments) {
			log(LOG_LEVEL_DEBUG, format, arguments);

		}

		@Override
		public void debug(String msg, Throwable t) {
			log(LOG_LEVEL_DEBUG, msg, t);

		}

		@Override
		public boolean isDebugEnabled(Marker marker) {
			return isDebugEnabled();
		}

		@Override
		public void debug(Marker marker, String msg) {
			log(LOG_LEVEL_DEBUG, msg);

		}

		@Override
		public void debug(Marker marker, String format, Object arg) {
			log(LOG_LEVEL_DEBUG, format, arg);

		}

		@Override
		public void debug(Marker marker, String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_DEBUG, format, arg1, arg2);

		}

		@Override
		public void debug(Marker marker, String format, Object... arguments) {
			log(LOG_LEVEL_DEBUG, format, arguments);

		}

		@Override
		public void debug(Marker marker, String msg, Throwable t) {
			log(LOG_LEVEL_DEBUG, msg, t);

		}

		@Override
		public boolean isInfoEnabled() {
			return level <= LOG_LEVEL_INFO;
		}

		@Override
		public void info(String msg) {
			log(LOG_LEVEL_INFO, msg);

		}

		@Override
		public void info(String format, Object arg) {
			log(LOG_LEVEL_INFO, format, arg);

		}

		@Override
		public void info(String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_INFO, format, arg1, arg2);

		}

		@Override
		public void info(String format, Object... arguments) {
			log(LOG_LEVEL_INFO, format, arguments);

		}

		@Override
		public void info(String msg, Throwable t) {
			log(LOG_LEVEL_INFO, msg, t);

		}

		@Override
		public boolean isInfoEnabled(Marker marker) {
			return isInfoEnabled();
		}

		@Override
		public void info(Marker marker, String msg) {
			log(LOG_LEVEL_INFO, msg);

		}

		@Override
		public void info(Marker marker, String format, Object arg) {
			log(LOG_LEVEL_INFO, format, arg);

		}

		@Override
		public void info(Marker marker, String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_INFO, format, arg1, arg2);

		}

		@Override
		public void info(Marker marker, String format, Object... arguments) {
			log(LOG_LEVEL_INFO, format, arguments);

		}

		@Override
		public void info(Marker marker, String msg, Throwable t) {
			log(LOG_LEVEL_INFO, msg, t);

		}

		@Override
		public boolean isWarnEnabled() {
			return level <= LOG_LEVEL_WARN;
		}

		@Override
		public void warn(String msg) {
			log(LOG_LEVEL_WARN, msg);

		}

		@Override
		public void warn(String format, Object arg) {
			log(LOG_LEVEL_WARN, format, arg);

		}

		@Override
		public void warn(String format, Object... arguments) {
			log(LOG_LEVEL_WARN, format, arguments);

		}

		@Override
		public void warn(String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_WARN, format, arg1, arg2);

		}

		@Override
		public void warn(String msg, Throwable t) {
			log(LOG_LEVEL_WARN, msg);

		}

		@Override
		public boolean isWarnEnabled(Marker marker) {
			return isWarnEnabled();
		}

		@Override
		public void warn(Marker marker, String msg) {
			log(LOG_LEVEL_WARN, msg);

		}

		@Override
		public void warn(Marker marker, String format, Object arg) {
			log(LOG_LEVEL_WARN, format, arg);

		}

		@Override
		public void warn(Marker marker, String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_WARN, format, arg1, arg2);

		}

		@Override
		public void warn(Marker marker, String format, Object... arguments) {
			log(LOG_LEVEL_WARN, format, arguments);

		}

		@Override
		public void warn(Marker marker, String msg, Throwable t) {
			log(LOG_LEVEL_WARN, msg, t);

		}

		@Override
		public boolean isErrorEnabled() {
			return level <= LOG_LEVEL_ERROR;
		}

		@Override
		public void error(String msg) {
			log(LOG_LEVEL_ERROR, msg);

		}

		@Override
		public void error(String format, Object arg) {
			log(LOG_LEVEL_ERROR, format, arg);

		}

		@Override
		public void error(String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_ERROR, format, arg1, arg2);

		}

		@Override
		public void error(String format, Object... arguments) {
			log(LOG_LEVEL_ERROR, format, arguments);

		}

		@Override
		public void error(String msg, Throwable t) {
			log(LOG_LEVEL_ERROR, msg + ": {}", t);

		}

		@Override
		public boolean isErrorEnabled(Marker marker) {
			return isErrorEnabled();
		}

		@Override
		public void error(Marker marker, String msg) {
			log(LOG_LEVEL_ERROR, msg);

		}

		@Override
		public void error(Marker marker, String format, Object arg) {
			log(LOG_LEVEL_ERROR, format, arg);

		}

		@Override
		public void error(Marker marker, String format, Object arg1, Object arg2) {
			log(LOG_LEVEL_ERROR, format, arg1, arg2);

		}

		@Override
		public void error(Marker marker, String format, Object... arguments) {
			log(LOG_LEVEL_ERROR, format, arguments);

		}

		@Override
		public void error(Marker marker, String msg, Throwable t) {
			log(LOG_LEVEL_ERROR, msg, t);

		}

	}

	static class NullLogger implements Logger {
		private String name;

		public NullLogger(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		@Override
		public void trace(String msg) {

		}

		@Override
		public void trace(String format, Object arg) {

		}

		@Override
		public void trace(String format, Object arg1, Object arg2) {

		}

		@Override
		public void trace(String format, Object... arguments) {

		}

		@Override
		public void trace(String msg, Throwable t) {

		}

		@Override
		public boolean isTraceEnabled(Marker marker) {
			return false;
		}

		@Override
		public void trace(Marker marker, String msg) {

		}

		@Override
		public void trace(Marker marker, String format, Object arg) {

		}

		@Override
		public void trace(Marker marker, String format, Object arg1, Object arg2) {

		}

		@Override
		public void trace(Marker marker, String format, Object... argArray) {

		}

		@Override
		public void trace(Marker marker, String msg, Throwable t) {

		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void debug(String msg) {

		}

		@Override
		public void debug(String format, Object arg) {

		}

		@Override
		public void debug(String format, Object arg1, Object arg2) {

		}

		@Override
		public void debug(String format, Object... arguments) {

		}

		@Override
		public void debug(String msg, Throwable t) {

		}

		@Override
		public boolean isDebugEnabled(Marker marker) {
			return false;
		}

		@Override
		public void debug(Marker marker, String msg) {

		}

		@Override
		public void debug(Marker marker, String format, Object arg) {

		}

		@Override
		public void debug(Marker marker, String format, Object arg1, Object arg2) {

		}

		@Override
		public void debug(Marker marker, String format, Object... arguments) {

		}

		@Override
		public void debug(Marker marker, String msg, Throwable t) {

		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public void info(String msg) {

		}

		@Override
		public void info(String format, Object arg) {

		}

		@Override
		public void info(String format, Object arg1, Object arg2) {

		}

		@Override
		public void info(String format, Object... arguments) {

		}

		@Override
		public void info(String msg, Throwable t) {

		}

		@Override
		public boolean isInfoEnabled(Marker marker) {
			return false;
		}

		@Override
		public void info(Marker marker, String msg) {

		}

		@Override
		public void info(Marker marker, String format, Object arg) {

		}

		@Override
		public void info(Marker marker, String format, Object arg1, Object arg2) {

		}

		@Override
		public void info(Marker marker, String format, Object... arguments) {

		}

		@Override
		public void info(Marker marker, String msg, Throwable t) {

		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void warn(String msg) {

		}

		@Override
		public void warn(String format, Object arg) {

		}

		@Override
		public void warn(String format, Object... arguments) {

		}

		@Override
		public void warn(String format, Object arg1, Object arg2) {

		}

		@Override
		public void warn(String msg, Throwable t) {

		}

		@Override
		public boolean isWarnEnabled(Marker marker) {
			return false;
		}

		@Override
		public void warn(Marker marker, String msg) {

		}

		@Override
		public void warn(Marker marker, String format, Object arg) {

		}

		@Override
		public void warn(Marker marker, String format, Object arg1, Object arg2) {

		}

		@Override
		public void warn(Marker marker, String format, Object... arguments) {

		}

		@Override
		public void warn(Marker marker, String msg, Throwable t) {

		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public void error(String msg) {

		}

		@Override
		public void error(String format, Object arg) {

		}

		@Override
		public void error(String format, Object arg1, Object arg2) {

		}

		@Override
		public void error(String format, Object... arguments) {

		}

		@Override
		public void error(String msg, Throwable t) {

		}

		@Override
		public boolean isErrorEnabled(Marker marker) {
			return false;
		}

		@Override
		public void error(Marker marker, String msg) {

		}

		@Override
		public void error(Marker marker, String format, Object arg) {

		}

		@Override
		public void error(Marker marker, String format, Object arg1, Object arg2) {

		}

		@Override
		public void error(Marker marker, String format, Object... arguments) {

		}

		@Override
		public void error(Marker marker, String msg, Throwable t) {

		}

	}
}
