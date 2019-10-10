package com.rapid7.log4j;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogentriesAppenderTest {

    private static final String token = "some-token";
    private static final String region = "some-region";
    private static final String location = "some location";
    private static final String accountKey = "account key";
    private static final String facility = "DAEMON";

    @Test
    public void settersTest() {
        LogentriesAppender le = new LogentriesAppender();
        le.setHttpPut(true);
        le.setToken(token);
        le.setRegion(region);
        le.setLocation(location);
        le.setKey(accountKey);
        le.setSsl(true);
        assertEquals(le.iopsAsync.getToken(), token);
        assertEquals(le.iopsAsync.getRegion(), region);
        assertEquals(le.iopsAsync.getHttpPut(), true);
        assertEquals(le.iopsAsync.getKey(), accountKey);
        assertEquals(le.iopsAsync.getLocation(), location);
        assertEquals(le.iopsAsync.getSsl(), true);
    }

}
