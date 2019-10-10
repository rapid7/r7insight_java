package com.rapid7.jul;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class LogentriesHandlerTest {

    private String trustStorePropertyValue;

    private final String TRUST_STORE_PROPERTY_NAME = "javax.net.ssl.trustStore";

    @Before
    public void setUpSSLCertificate() {
        trustStorePropertyValue = System.getProperty(TRUST_STORE_PROPERTY_NAME);
        System.setProperty(TRUST_STORE_PROPERTY_NAME, getClass().getClassLoader().getResource("unit_test_key_store.jks").getPath());
    }

    @After
    public void cleanUpConfiguration() {
        if (trustStorePropertyValue == null) {
            System.clearProperty(TRUST_STORE_PROPERTY_NAME);
        } else {
            System.setProperty(TRUST_STORE_PROPERTY_NAME, trustStorePropertyValue);
        }
        LogManager.getLogManager().reset();
    }

    @Test
    public void singleLoggerDefaultTsl() throws Exception {
        final String message = "a message to log in tls";
        final String token = "0c7407d4-fd0d-4436-bb50-44f1266b4490";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(10000, true);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_single_handler_tls.properties"));
            Logger logger = Logger.getLogger("logger");
            logger.info(message);
            receiver.pollMessage();// skipping library init
            validateLogMessage(token, message, receiver.pollMessage());
        } finally {
            receiver.close();
        }
    }

    @Test
    public void singleLoggerNoTsl() throws Exception {
        final String message = "a message to log";
        final String token = "0c7407d4-fd0d-4436-bb50-44f1266b4490";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(10000, false);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_single_handler.properties"));
            Logger logger = Logger.getLogger("logger");
            logger.info(message);
            receiver.pollMessage();// skipping library init
            validateLogMessage(token, message, receiver.pollMessage());
        } finally {
            receiver.close();
        }
    }

    @Test
    public void multipleLoggers() throws Exception {
        final String tokenLogger1 = "4ff1cb0a-beea-4616-b647-1c113de8e7bb";
        final String tokenLogger2 = "a70d9089-576c-4668-9641-14995d493a62";
        final String messageLogger1 = "test message for logger_1";
        final String messageLogger2 = "test message for logger_2";
        SocketChannelReceiver receiverLogger1 = null;
        SocketChannelReceiver receiverLogger2 = null;
        try {
            receiverLogger1 = SocketChannelReceiver.createAndStartReceiver(10000, false);
            receiverLogger2 = SocketChannelReceiver.createAndStartReceiver(10001, false);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_multiple_handlers.properties"));
            Logger log1 = Logger.getLogger("logger1");
            Logger log2 = Logger.getLogger("logger2");
            log1.info(messageLogger1);
            receiverLogger1.pollMessage();// skipping library init
            validateLogMessage(tokenLogger1, messageLogger1, receiverLogger1.pollMessage());
            log2.info(messageLogger2);
            receiverLogger2.pollMessage();// skipping library init
            validateLogMessage(tokenLogger2, messageLogger2, receiverLogger2.pollMessage());
        } finally {
            receiverLogger1.close();
            receiverLogger2.close();
        }
    }


    private void validateLogMessage(String token, String message, String logLine) {
        assertTrue("Log line length verification", logLine.length() > token.length() + message.length());
        assertEquals("Token verification", token, logLine.substring(0, token.length()));
        assertEquals("Log Message verification", message, logLine.substring(logLine.length() - message.length() - 1, logLine.length() - 1));
    }

}
