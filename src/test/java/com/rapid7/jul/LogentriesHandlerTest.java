package com.rapid7.jul;


import org.junit.After;
import org.junit.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertEquals;

public class LogentriesHandlerTest {

    @After
    public void resetLogger() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void singleLogger() throws Exception {
        final String message = "single logger test";
        final String token = "2cb90b70-e5c8-436b-a7b1-15628325925c";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(10000);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_single_handler.properties"));
            Logger logger = Logger.getLogger("logger");
            logger.info(message);
            validateLogMessage(token, message, receiver.pollMessage());
        } finally {
            receiver.close();
        }
    }

    @Test
    public void multipleLoggers() throws Exception {
        final String tokenLogger1 = "token-logger-1";
        final String tokenLogger2 = "token-logger-2";
        final String messageLogger1 = "test message for logger_1";
        final String messageLogger2 = "test message for logger_2";
        SocketChannelReceiver receiverLogger1 = null;
        SocketChannelReceiver receiverLogger2 = null;
        try {
            receiverLogger1 = SocketChannelReceiver.createAndStartReceiver(10000);
            receiverLogger2 = SocketChannelReceiver.createAndStartReceiver(10001);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_multiple_handlers.properties"));
            Logger log1 = Logger.getLogger("logger1");
            Logger log2 = Logger.getLogger("logger2");
            log1.info(messageLogger1);
            validateLogMessage(tokenLogger1, messageLogger1, receiverLogger1.pollMessage());
            log2.info(messageLogger2);
            validateLogMessage(tokenLogger2, messageLogger2, receiverLogger2.pollMessage());
        } finally {
            receiverLogger1.close();
            receiverLogger2.close();
        }
    }

    private void validateLogMessage(String token, String message, String logLine) {
        assertEquals("Token verification", token, logLine.split(" ")[0]);
        assertEquals("Log Message verification", message, logLine.substring(logLine.length() - message.length() - 1, logLine.length() - 1));
    }

}
