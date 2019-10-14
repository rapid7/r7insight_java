package com.rapid7.log4j2;

import com.rapid7.net.AsyncLogger;
import com.rapid7.net.LoggerConfiguration;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Responsible for managing the actual connection to Logentries.
 * Created by josh on 11/15/14.
 */
public class LogentriesManager extends AbstractManager {
    private static LogentriesManagerFactory FACTORY = new LogentriesManagerFactory();
    private final AsyncLogger asyncLogger;

    protected LogentriesManager(LoggerContext loggerContext, String name, LoggerConfiguration data) {
        super(loggerContext, name);
        asyncLogger = new AsyncLogger(data);
        LOGGER.debug("AsyncLogger created.");
    }

    static LogentriesManager getManager(String name, LoggerConfiguration data) {
        return getManager(name, FACTORY, data);
    }

    @Override
    protected boolean releaseSub(final long timeout, final TimeUnit timeUnit) {
        asyncLogger.close();
        LOGGER.debug("AsyncLogger closed.");
        return true;
    }

    public void writeLine(String line) {
        asyncLogger.addLineToQueue(line);
    }

    static class LogentriesManagerFactory implements ManagerFactory<LogentriesManager, LoggerConfiguration> {
        @Override
        public LogentriesManager createManager(String name, LoggerConfiguration data) {
            return new LogentriesManager(new LoggerContext(name), name, data);
        }
    }

}
