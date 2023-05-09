package com.rapid7.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogMessageValidator {

    public static void validateLogMessage(String token, String content, String message, String logLine) {
        assertTrue(logLine.length() > token.length() + message.length() + content.length(), "Log line length verification");
        assertTrue(logLine.contains(content), "Content verification");
        assertEquals(token, logLine.substring(0, token.length()), "Token verification");
        assertEquals(message, logLine.substring(logLine.length() - message.length()), "Log Message verification");
    }

}
