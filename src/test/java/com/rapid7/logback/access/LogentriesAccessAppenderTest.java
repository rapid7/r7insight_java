package com.rapid7.logback.access;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.ServerAdapter;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.EchoLayout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

public class LogentriesAccessAppenderTest {

    private static final String token = "some-token";
    private static final String region = "some-region";
    private static final String location = "some location";
    private static final String accountKey = "account key";

    @Test
    public void setterTests() {
        LogentriesAccessAppender le = buildLogentriesAccessAppender();
        le.start();
        assertEquals(le.iopsAsync.getToken(), token);
        assertEquals(le.iopsAsync.getRegion(), region);
        assertEquals(le.iopsAsync.getLocation(), location);
        assertEquals(le.iopsAsync.getKey(), accountKey);
    }

    @Test
    public void testSetCustomLayout() {
        Layout<IAccessEvent> layout = mock(EchoLayout.class);
        AccessEvent accessEvent = buildAccessEvent();

        when(layout.doLayout(accessEvent)).thenReturn("formattedText");

        LogentriesAccessAppender logentriesAccessAppender = buildLogentriesAccessAppender();
        logentriesAccessAppender.setLayout(layout);
        logentriesAccessAppender.start();
        logentriesAccessAppender.append(accessEvent);

        verify(layout).doLayout(eq(accessEvent));
    }

    private AccessEvent buildAccessEvent() {
        AccessContext accessContext = new AccessContext();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServerAdapter adapter = mock(ServerAdapter.class);

        when(adapter.getContentLength()).thenReturn(123L);
        when(adapter.getRequestTimestamp()).thenReturn(System.currentTimeMillis());
        when(adapter.getStatusCode()).thenReturn(200);
        when(adapter.buildResponseHeaderMap()).thenReturn(new HashMap<String, String>() {{
            put("Content-Type", "text/html; charset=UTF-8");
            put("Content-Length", "42");
        }});
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/my/uri");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://123.123.123.123/my/uri"));
        when(request.getRemoteUser()).thenReturn("remote-user");
        when(request.getRemoteHost()).thenReturn("123.123.123.123");
        when(request.getProtocol()).thenReturn("HTTPS");

        return new AccessEvent(accessContext, request, response, adapter);
    }

    private LogentriesAccessAppender buildLogentriesAccessAppender() {
        LogentriesAccessAppender le = new LogentriesAccessAppender();
        le.setHttpPut(true);
        le.setToken(token);
        le.setRegion(region);
        le.setKey(accountKey);
        le.setLocation(location);
        le.setSsl(true);
        return le;
    }

}
