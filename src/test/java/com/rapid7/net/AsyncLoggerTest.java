package com.rapid7.net;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class AsyncLoggerTest {

    private static final String VALID_UUID = "a7ac14c3-2cc9-4f09-8fb3-73c5523e065c";

    private final AsyncLogger TEST_LOGGER = new AsyncLogger(
            new LoggerConfiguration.Builder()
                    .useToken("LOGENTRIES_TOKEN")
                    .build());

    @Test
    public void testConfigurationNoParams() {
        AsyncLogger async = new AsyncLogger(new LoggerConfiguration.Builder().build());
        assertEquals(async.getToken(), "", "token should be empty string by default");
        assertEquals(async.getRegion(), null, "region should be empty string by default");
        assertFalse(async.getHttpPut(), "httpput should be false by default");
        assertTrue(async.getSsl(), "ssl should be false by default");
        assertEquals(async.getKey(), null, "key should be empty string by default");
        assertEquals(async.getLocation(), "", "locaton should be empty string by default");
        assertEquals(async.getLogMessagePrefix(), "", "log prefix should be empty string by default");
        assertFalse(async.getUseDataHub(), "do not use datahub as default");
        assertEquals(async.getHostName(), null, "hostname should be empty");
    }

    @Test
    public void testLogPrefix() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .logHostNameAsPrefix(true)
                        .setLogIdPrefix("LogId")
                        .useAsHostName("localhost")
                        .build());
        assertEquals(async.getLogMessagePrefix(), "LogId HostName=localhost ", "log prefix should be set");
    }

    @Test
    public void testLogPrefixLogIdandHostNoSpecified() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .logHostNameAsPrefix(true)
                        .build());
        assertTrue(async.getLogMessagePrefix().contains("HostName="), "log prefix should be set");
    }

    @Test
    public void testOversizeMessage() {
        AsyncLogger async = new AsyncLogger(new LoggerConfiguration.Builder().build());
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 2100000; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        async.addLineToQueue(output);
    }


    @Test
    public void testCheckCredentialsMissingToken() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken("LOGENTRIES_TOKEN")
                        .build());
        assertFalse(async.checkCredentials(), "checkCredentials should return false for default token string");
    }

    @Test
    public void testCheckCredentialsMissingRegion() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken(VALID_UUID)
                        .build());
        assertFalse(async.checkCredentials(), "checkCredentials should return false for null region");
    }

    @Test
    public void testCheckCredentialsValidToken() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken(VALID_UUID)
                        .inRegion("eu")
                        .build());
        assertTrue(async.checkCredentials(),"checkCredentials should return true for valid token");
    }

    @Test
    public void testCheckCredentialsMissingKey() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useHttpPut(true)
                        .httpPutLocation("location")
                        .build());
        assertFalse(async.checkCredentials(),"checkCredentials should return false for missing key");
    }

    @Test
    public void testCheckCredentialsInvalidKey() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useHttpPut(true)
                        .inRegion("someRegion")
                        .useAccountKey("not-a-uuid")
                        .httpPutLocation("anywhere")
                        .build());
        assertFalse(async.checkCredentials(), "checkCredentials should return false for invalid key");
    }

    @Test
    public void testCheckCredentialsValidKey() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useHttpPut(true)
                        .inRegion("someRegion")
                        .useAccountKey(VALID_UUID)
                        .httpPutLocation("anywhere")
                        .build());
        assertTrue(async.checkCredentials(), "checkCredentials should return true for valid key");
    }


    @Test
    public void testCheckValidUUID() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken("LOGENTRIES_TOKEN")
                        .build());
        assertFalse(async.checkValidUUID(""), "checkValidUUID should return false for an empty string");
        assertFalse(async.checkValidUUID("not-a-uuid"), "checkValidUUID should return false for invalid uuid");
        assertTrue(async.checkValidUUID(VALID_UUID), "checkValidUUID should return true for valid uuid");
    }

    @Test
    public void testOpenConnectionIOExceptionIsNotMasked() throws Exception {
        InsightOpsClient client = mock(InsightOpsClient.class);

        assertThrows(IOException.class, () -> {
            doThrow(IOException.class).when(client).connect();
            TEST_LOGGER.getAppender().iopsClient = client;
            TEST_LOGGER.getAppender().openConnection();
        });
    }

    @Test
    public void testReopenConnectionExceptionThrown() throws Exception {
        InsightOpsClient client = mock(InsightOpsClient.class);

        assertThrows(Exception.class, () -> {
            doThrow(RuntimeException.class).when(client).connect();
            TEST_LOGGER.getAppender().iopsClient = client;
            TEST_LOGGER.getAppender().reopenConnection();
        });
    }
}
