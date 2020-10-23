package com.rapid7.net;

import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        assertEquals("token should be empty string by default", async.getToken(), "");
        assertEquals("region should be empty string by default", async.getRegion(), null);
        assertFalse("httpput should be false by default", async.getHttpPut());
        assertTrue("ssl should be false by default", async.getSsl());
        assertEquals("key should be empty string by default", async.getKey(), null);
        assertEquals("locaton should be empty string by default", async.getLocation(), "");
        assertEquals("log prefix should be empty string by default", async.getLogMessagePrefix(), "");
        assertFalse("do not use datahub as default", async.getUseDataHub());
        assertEquals("hostname should be empty", async.getHostName(), null);
    }

    @Test
    public void testLogPrefix() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .logHostNameAsPrefix(true)
                        .setLogIdPrefix("LogId")
                        .useAsHostName("localhost")
                        .build());
        assertEquals("log prefix should be set", async.getLogMessagePrefix(), "LogId HostName=localhost ");
    }

    @Test
    public void testLogPrefixLogIdandHostNoSpecified() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .logHostNameAsPrefix(true)
                        .build());
        assertTrue("log prefix should be set", async.getLogMessagePrefix().contains("HostName="));
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
        assertFalse("checkCredentials should return false for default token string", async.checkCredentials());
    }

    @Test
    public void testCheckCredentialsMissingRegion() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken(VALID_UUID)
                        .build());
        assertFalse("checkCredentials should return false for null region", async.checkCredentials());
    }

    @Test
    public void testCheckCredentialsValidToken() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken(VALID_UUID)
                        .inRegion("eu")
                        .build());
        assertTrue("checkCredentials should return true for valid token", async.checkCredentials());
    }

    @Test
    public void testCheckCredentialsMissingKey() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useHttpPut(true)
                        .httpPutLocation("location")
                        .build());
        assertFalse("checkCredentials should return false for missing key", async.checkCredentials());
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
        assertFalse("checkCredentials should return false for invalid key", async.checkCredentials());
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
        assertTrue("checkCredentials should return true for valid key", async.checkCredentials());
    }


    @Test
    public void testCheckValidUUID() {
        AsyncLogger async = new AsyncLogger(
                new LoggerConfiguration.Builder()
                        .useToken("LOGENTRIES_TOKEN")
                        .build());
        assertFalse("checkValidUUID should return false for an empty string", async.checkValidUUID(""));
        assertFalse("checkValidUUID should return false for invalid uuid", async.checkValidUUID("not-a-uuid"));
        assertTrue("checkValidUUID should return true for valid uuid", async.checkValidUUID(VALID_UUID));
    }

    @Test(expected = IOException.class)
    public void testOpenConnectionIOExceptionIsNotMasked() throws Exception {
        InsightOpsClient client = mock(InsightOpsClient.class);
        doThrow(IOException.class).when(client).connect();

        TEST_LOGGER.getAppender().iopsClient = client;
        TEST_LOGGER.getAppender().openConnection();
    }

    @Test(expected = Exception.class)
    public void testReopenConnectionExceptionThrown() throws Exception {
        InsightOpsClient client = mock(InsightOpsClient.class);
        doThrow(RuntimeException.class).when(client).connect();

        TEST_LOGGER.getAppender().iopsClient = client;
        TEST_LOGGER.getAppender().reopenConnection();
    }
}
