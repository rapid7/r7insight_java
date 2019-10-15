package com.rapid7.jul;

import com.rapid7.net.AsyncLogger;
import com.rapid7.net.LoggerConfiguration;

import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static java.util.logging.ErrorManager.FORMAT_FAILURE;
import static java.util.logging.ErrorManager.GENERIC_FAILURE;

public class LogentriesHandler extends Handler {

    /**
     * Asynchronous Background logger
     */
    final AsyncLogger iopsAsync;

    public LogentriesHandler() {
        this(null);
    }

    public LogentriesHandler(String prefix) {
        iopsAsync = new AsyncLogger(loadConfiguration(prefix));
    }

    private LoggerConfiguration loadConfiguration(String prefix) {
        String cname = getClass().getName();
        String propsPrefix = prefix == null ? cname : prefix + "." + cname;
        setLevel(getLevelProperty(propsPrefix + ".level", Level.INFO));
        setFormatter(getFormatterProperty(propsPrefix + ".formatter", new SimpleFormatter()));
        return new LoggerConfiguration.Builder()
                .inRegion(getStringProperty(propsPrefix + ".region", ""))
                .toServerAddress(getStringProperty(propsPrefix + ".host", null))
                .toServerPort(getIntProperty(propsPrefix + ".port", 0))
                .useToken(getStringProperty(propsPrefix + ".token", ""))
                .useDataHub(getBooleanProperty(propsPrefix + ".useDataHub", false))
                .useHttpPut(getBooleanProperty(propsPrefix + ".httpPut", false))
                .useAccountKey(getStringProperty(propsPrefix + ".key", ""))
                .httpPutLocation(getStringProperty(propsPrefix + ".location", ""))
                .runInDebugMode(getBooleanProperty(propsPrefix + ".debug", false))
                .logHostNameAsPrefix(getBooleanProperty(propsPrefix + ".logHostName", false))
                .useAsHostName(getStringProperty(propsPrefix + ".hostNameToLog", ""))
                .setLogIdPrefix(getStringProperty(propsPrefix + ".logId", ""))
                .useSSL(getBooleanProperty(propsPrefix + ".ssl", true))
                .build();
    }


    @Override
    public synchronized void publish(LogRecord record) {
        if (isLoggable(record)) {
            this.iopsAsync.addLineToQueue(formatMessage(record));
        }
    }

    String formatMessage(LogRecord record) {
        String msg = "";
        try {
            msg = getFormatter().format(record);
            // replace line separators with unicode equivalent
            msg = msg.replace(System.getProperty("line.separator"), "\u2028");
        } catch (Exception e) {
            reportError("Error while formatting.", e, FORMAT_FAILURE);
        }
        return msg;
    }

    @Override
    public void flush() {
        // no need to flush since the log is sent by AsyncLogger
    }

    @Override
    public void close() {
        iopsAsync.close();
    }

    // -- These methods are private in LogManager

    Level getLevelProperty(String name, Level defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.parse(val.trim());
        return l != null ? l : defaultValue;
    }

    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        try {
            if (val != null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class<?> clz = cl.loadClass(val);
                return (Formatter) clz.newInstance();
            }
        } catch (ClassNotFoundException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name), e, GENERIC_FAILURE);
        } catch (InstantiationException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name), e, GENERIC_FAILURE);
        } catch (IllegalAccessException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name), e, GENERIC_FAILURE);
        }
        return defaultValue;
    }

    String getStringProperty(String name, String defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        if ("false".equalsIgnoreCase(val.trim())) {
            return false;
        } else if ("true".equalsIgnoreCase(val.trim())) {
            return true;
        } else {
            reportError(MessageFormat.format("Error reading property ''{0}''", name), null, GENERIC_FAILURE);
            return defaultValue;
        }
    }

    int getIntProperty(String name, int defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name), e, GENERIC_FAILURE);
            return defaultValue;
        }
    }

}
