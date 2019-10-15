package com.rapid7.log4j;

import com.rapid7.util.SocketChannelReceiver;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import static com.rapid7.util.LogMessageValidator.validateLogMessage;
import static junit.framework.TestCase.assertEquals;

public class LogentriesAppenderTest {

    private static final String token = "some-token";
    private static final String region = "some-region";
    private static final String location = "some location";
    private static final String accountKey = "account key";
    private static final String address = "192.168.0.2";
    private static final boolean debug = true;
    private static final String hostname = "server1";
    private static final String logId = "LogId";
    private static final int port = 1000;

    private static final String EMPTY_PREFIX = "";

    @Test
    public void settersTest() {
        LogentriesAppender le = new LogentriesAppender();
        le.setHttpPut(true);
        le.setToken(token);
        le.setRegion(region);
        le.setLocation(location);
        le.setKey(accountKey);
        le.setSsl(true);
        le.setDataHubAddr(address);
        le.setDataHubPort(port);
        le.setDebug(debug);
        le.setHostName(hostname);
        le.setLogHostName(true);
        le.setLogID(logId);
        le.activateOptions();
        assertEquals(le.iopsAsync.getToken(), token);
        assertEquals(le.iopsAsync.getRegion(), region);
        assertEquals(le.iopsAsync.getHttpPut(), true);
        assertEquals(le.iopsAsync.getKey(), accountKey);
        assertEquals(le.iopsAsync.getLocation(), location);
        assertEquals(le.iopsAsync.getSsl(), true);
        assertEquals(le.iopsAsync.getDataHubAddr(), address);
        assertEquals(le.iopsAsync.getDataHubPort(), port);
        assertEquals(le.iopsAsync.getHostName(), hostname);
        assertEquals(le.iopsAsync.getLogID(), logId);
        assertEquals(le.iopsAsync.getDebug(), debug);
    }

    @Test
    public void testLog() throws Exception {
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("log4j.properties"));
        final String message = "a message to log using log4j in tls mode";
        final String token = "0c7407d4-fd0d-4436-bb50-44f1266b4490";
        final String exceptionMessage = "java.lang.RuntimeException";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(20000, true);
            Logger log = LogManager.getRootLogger();
            log.info(message);
            receiver.pollMessage();// skipping library init
            validateLogMessage(token, EMPTY_PREFIX, message, receiver.pollMessage());
            log.error("failure", new RuntimeException("error").initCause(new RuntimeException("cause")));
            validateLogMessage(token, exceptionMessage, EMPTY_PREFIX, receiver.pollMessage());
        } finally {
            receiver.close();
        }
    }

}
