package com.rapid7.log4j2;

import com.rapid7.util.SocketChannelReceiver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.rapid7.util.LogMessageValidator.validateLogMessage;

/**
 * Test the Log4J2 appender.
 * Created by josh on 11/15/14.
 */
public class Log4J2Test {
    private LoggerContext loggerContext;
    private static final String EMPTY_PREFIX = "";

    @Before
    public void setUp() {
        loggerContext = Configurator.initialize("test-log4j2", "log4j2-appender-test.xml");
    }

    @After
    public void tearDown() {
        Configurator.shutdown(loggerContext);
    }


    @Test
    public void testLog() throws Exception {
        final String message = "a message to log using log4j2 in tls mode";
        final String token = "0c7407d4-fd0d-4436-bb50-44f1266b4490";
        SocketChannelReceiver receiver = null;
        try {
            receiver = SocketChannelReceiver.createAndStartReceiver(20000, true);
            Logger log = LogManager.getLogger("test-async-logger");
            log.info(message);
            receiver.pollMessage();// skipping library init
            validateLogMessage(token, EMPTY_PREFIX, message, receiver.pollMessage());
        } finally {
            receiver.close();
        }
    }


}
