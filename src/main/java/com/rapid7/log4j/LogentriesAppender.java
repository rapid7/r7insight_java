package com.rapid7.log4j;

import com.rapid7.net.AsyncLogger;
import com.rapid7.net.LoggerConfiguration;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Logentries appender for log4j.
 *
 * @author Mark Lacomber
 */
public class LogentriesAppender extends AppenderSkeleton {

    /*
     * Fields
     */
    /**
     * Asynchronous Background logger
     */
    AsyncLogger iopsAsync;

    private final LoggerConfiguration.Builder configurationBuilder;

    public LogentriesAppender() {
        configurationBuilder = new LoggerConfiguration.Builder();
    }

    /*
     * Public methods to send log4j parameters to AsyncLogger
     */

    /**
     * Sets the token
     *
     * @param token
     */
    public void setToken(String token) {
        this.configurationBuilder.useToken(token);
    }

    /**
     * Sets the region
     *
     * @param region
     */
    public void setRegion(String region) {
        this.configurationBuilder.inRegion(region);
    }

    /**
     * Sets the HTTP PUT boolean flag. Send logs via HTTP PUT instead of default Token TCP
     *
     * @param httpPut HttpPut flag to set
     */
    public void setHttpPut(boolean httpPut) {
        this.configurationBuilder.useHttpPut(httpPut);
    }

    /**
     * Sets the ACCOUNT KEY value for HTTP PUT
     *
     * @param accountKey
     */
    public void setKey(String accountKey) {
        this.configurationBuilder.useAccountKey(accountKey);
    }

    /**
     * Sets the LOCATION value for HTTP PUT
     *
     * @param logLocation
     */
    public void setLocation(String logLocation) {
        this.configurationBuilder.httpPutLocation(logLocation);
    }

    /**
     * Sets the SSL boolean flag
     *
     * @param ssl
     */
    public void setSsl(boolean ssl) {
        this.configurationBuilder.useSSL(ssl);
    }

    /**
     * Sets the debug flag. Appender in debug mode will print error messages on
     * error console.
     *
     * @param debug debug flag to set
     */
    public void setDebug(boolean debug) {
        this.configurationBuilder.runInDebugMode(debug);
    }

    /**
     * Sets the flag which determines if DataHub instance is used instead of Logentries service.
     *
     * @param useDataHub set to true to send log messaged to a DataHub instance.
     */
    public void setIsUsingDataHub(boolean useDataHub) {
        this.configurationBuilder.useDataHub(useDataHub);
    }

    /**
     * Sets the address where DataHub server resides.
     *
     * @param dataHubAddr address like "127.0.0.1"
     */
    public void setDataHubAddr(String dataHubAddr) {
        this.configurationBuilder.toServerAddress(dataHubAddr);
    }

    /**
     * Sets the port number on which DataHub instance waits for log messages.
     *
     * @param dataHubPort
     */
    public void setDataHubPort(int dataHubPort) {
        this.configurationBuilder.toServerPort(dataHubPort);
    }

    /**
     * Determines whether to send HostName alongside with the log message
     *
     * @param logHostName
     */
    public void setLogHostName(boolean logHostName) {
        this.configurationBuilder.logHostNameAsPrefix(logHostName);
    }

    /**
     * Sets the HostName from the configuration
     *
     * @param hostName
     */
    public void setHostName(String hostName) {
        this.configurationBuilder.useAsHostName(hostName);
    }

    /**
     * Sets LogID parameter from the configuration, it will appear as prefix in any log line
     *
     * @param logID
     */
    public void setLogID(String logID) {
        this.configurationBuilder.setLogIdPrefix(logID);
    }

    /**
     * Implements AppenderSkeleton Append method, handles time and format
     *
     * @event event to log
     */
    @Override
    protected void append(LoggingEvent event) {

        // Render the event according to layout
        String formattedEvent = layout.format(event);

        // Append stack trace if present and layout does not handle it
        if (layout.ignoresThrowable()) {
            String[] stack = event.getThrowableStrRep();
            if (stack != null) {
                int len = stack.length;
                formattedEvent += ", ";
                for (int i = 0; i < len; i++) {
                    formattedEvent += stack[i];
                    if (i < len - 1)
                        formattedEvent += "\u2028";
                }
            }
        }

        // Prepare to be queued
        this.iopsAsync.addLineToQueue(formattedEvent);
    }

    /**
     * Closes all connections to Logentries
     */
    @Override
    public void close() {
        this.iopsAsync.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    public void activateOptions() {
        iopsAsync = new AsyncLogger(configurationBuilder.build());
        super.activateOptions();
    }
}
