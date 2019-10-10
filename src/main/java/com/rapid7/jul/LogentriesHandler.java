package com.rapid7.jul;

import com.rapid7.net.AsyncLogger;

import java.text.MessageFormat;
import java.util.logging.*;

import static java.util.logging.ErrorManager.FORMAT_FAILURE;
import static java.util.logging.ErrorManager.GENERIC_FAILURE;

public class LogentriesHandler extends Handler {

    /**
     * Asynchronous Background logger
     */
    AsyncLogger iopsAsync;

    public LogentriesHandler() {
        this(null);
    }

    public LogentriesHandler(String prefix) {
        iopsAsync = new AsyncLogger();
        configure(prefix);
    }

    void configure(String prefix) {
        String cname = getClass().getName();
        String propsPrefix = prefix == null ? cname : prefix + "." + cname;
        setLevel(getLevelProperty(propsPrefix + ".level", Level.INFO));
        setFormatter(getFormatterProperty(propsPrefix + ".formatter", new SimpleFormatter()));
        setRegion(getStringProperty(propsPrefix + ".region", ""));
        setHost(getStringProperty(propsPrefix + ".host", null));
        setPort(getIntProperty(propsPrefix + ".port", 0));
        setToken(getStringProperty(propsPrefix + ".token", ""));
        setUseDataHub(getBooleanProperty(propsPrefix + ".useDataHub", false));
        setHttpPut(getBooleanProperty(propsPrefix + ".httpPut", false));
        setSsl(getBooleanProperty(propsPrefix + ".ssl", true));
    }

    private void setUseDataHub(boolean useDataHub) {
        iopsAsync.setUseDataHub(useDataHub);
    }

    private void setRegion(String region) {
        iopsAsync.setRegion(region);
    }


    private void setHost(String host) {
        iopsAsync.setDataHubAddr(host);
    }

    private void setPort(int port) {
        iopsAsync.setDataHubPort(port);
    }

    private void setToken(String token) {
        iopsAsync.setToken(token);
    }

    public void setSsl(boolean ssl) {
        this.iopsAsync.setSsl(ssl);
    }

    public void setHttpPut(boolean httpPut) {
        this.iopsAsync.setHttpPut(httpPut);
    }

    @Override
    public void publish(LogRecord record) {
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
        try {
            return Boolean.parseBoolean(val.trim());
        } catch (NumberFormatException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name), e, GENERIC_FAILURE);
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
