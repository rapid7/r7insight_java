package com.rapid7.jul;


import com.rapid7.util.SocketChannelReceiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.rapid7.util.LogMessageValidator.validateLogMessage;

public class LogentriesHandlerTest {

    @AfterEach
    public void cleanUpConfiguration() {
        LogManager.getLogManager().reset();
    }

    private static final String EMPTY_PREFIX = "";

    /**
     * This test needs the unit_test_key_store.jks certificate to be added to Trust Store, this is done in the pom.xml
     *
     * @throws Exception
     */
    @Test
    public void singleLoggerDefaultTls() throws Exception {
        final String message = "a message to log in tls";
        final String token = "0c7407d4-fd0d-4436-bb50-44f1266b4490";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(20000, true);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_single_handler_tls.properties"));
            Logger logger = Logger.getLogger("logger");
            logger.info(message);
            receiver.pollMessage();// skipping library init
            validateLogMessage(token, EMPTY_PREFIX, message, receiver.pollMessage());
        } finally {
            receiver.close();
        }
    }

    @Test
    public void singleLoggerNoTls() throws Exception {
        final String message = "a message to log";
        final String token = "0c7407d4-fd0d-4436-bb50-44f1266b4490";
        final String prefix = "aLogId HostName=localhost";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(10000, false);
            LogManager.getLogManager().readConfiguration(getClass()
                    .getClassLoader().getResourceAsStream("logging_single_handler.properties"));
            Logger logger = Logger.getLogger("logger");
            logger.info(message);
            receiver.pollMessage();// skipping library init
            validateLogMessage(token, prefix, message, receiver.pollMessage());
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
            validateLogMessage(tokenLogger1, EMPTY_PREFIX, messageLogger1, receiverLogger1.pollMessage());
            log2.info(messageLogger2);
            receiverLogger2.pollMessage();// skipping library init
            validateLogMessage(tokenLogger2, EMPTY_PREFIX, messageLogger2, receiverLogger2.pollMessage());
        } finally {
            receiverLogger1.close();
            receiverLogger2.close();
        }
    }

}
