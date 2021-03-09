package turtleduck.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class Logging {

	private static ILoggerFactory factory = null;
	private static boolean tried = false;

	public static ILoggerFactory getFactory() {
		if (!tried) {
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
			return new NullLogger(name);
		}
	}

	public static Logger getLogger(Class<?> clazz) {
		getFactory();
		if (factory != null) {
			return factory.getLogger(clazz.getName());
		} else {
			return new NullLogger(clazz.getName());
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
