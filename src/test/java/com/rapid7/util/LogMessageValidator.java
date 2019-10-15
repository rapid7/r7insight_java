package com.rapid7.util;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class LogMessageValidator {

    public static void validateLogMessage(String token, String content, String message, String logLine) {
        assertTrue("Log line length verification", logLine.length() > token.length() + message.length() + content.length());
        assertTrue("Content verification", logLine.contains(content));
        assertEquals("Token verification", token, logLine.substring(0, token.length()));
        assertEquals("Log Message verification", message, logLine.substring(logLine.length() - message.length()));
    }

}
