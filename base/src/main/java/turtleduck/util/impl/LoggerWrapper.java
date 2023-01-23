package turtleduck.util.impl;

import turtleduck.util.Logger;
import org.slf4j.Marker;

public class LoggerWrapper implements Logger {
    private org.slf4j.Logger logger;
    private boolean debug = false;

    public LoggerWrapper(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    public void debugEnabled(boolean enable) {
        debug = enable;
    }

    public String getName() {
        return logger.getName();
    }

    public boolean isTraceEnabled() {
        return debug && logger.isTraceEnabled();
    }

    public void trace(String msg) {
        if (debug)
            logger.trace(msg);
    }

    public void trace(String format, Object arg) {
        if (debug)
            logger.trace(format, arg);
    }

    public void trace(String format, Object arg1, Object arg2) {
        if (debug)
            logger.trace(format, arg1, arg2);
    }

    public void trace(String format, Object... arguments) {
        if (debug)
            logger.trace(format, arguments);
    }

    public void trace(String msg, Throwable t) {
        if (debug)
            logger.trace(msg, t);
    }

    public boolean isTraceEnabled(Marker marker) {
        return debug && logger.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg) {
        if (debug)
            logger.trace(marker, msg);
    }

    public void trace(Marker marker, String format, Object arg) {
        if (debug)
            logger.trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (debug)
            logger.trace(marker, format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object... argArray) {
        if (debug)
            logger.trace(marker, format, argArray);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        if (debug)
            logger.trace(marker, msg, t);
    }

    public boolean isDebugEnabled() {
        return debug && logger.isDebugEnabled();
    }

    public void debug(String msg) {
        if (debug)
            logger.debug(msg);
    }

    public void debug(String format, Object arg) {
        if (debug)
            logger.debug(format, arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        if (debug)
            logger.debug(format, arg1, arg2);
    }

    public void debug(String format, Object... arguments) {
        if (debug)
            logger.debug(format, arguments);
    }

    public void debug(String msg, Throwable t) {
        if (debug)
            logger.debug(msg, t);
    }

    public boolean isDebugEnabled(Marker marker) {
        return debug && logger.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        if (debug)
            logger.debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        if (debug)
            logger.debug(marker, format, arg);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (debug)
            logger.debug(marker, format, arg1, arg2);
    }

    public void debug(Marker marker, String format, Object... arguments) {
        if (debug)
            logger.debug(marker, format, arguments);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        if (debug)
            logger.debug(marker, msg, t);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
    }

    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, arguments);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void error(String msg) {
        logger.error(msg);
    }

    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, arguments);
    }

    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }
}
