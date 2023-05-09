package com.rapid7.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsightOpsClientTest {

    private static final String API_TOKEN_SERVER = "%s.data.logs.insight.rapid7.com";
    private static final String DATAHUB_IP = "127.0.0.1";
    private static final int IOPS_SSL_PORT = 443;
    private static final int IOPS_PORT = 80;
    private static final int DATAHUB_PORT = 10000;
    private static final boolean USE_HTTP_PUT = true;
    private static final boolean NOT_USE_HTTP_PUT = false;
    private static final boolean USE_SSL = true;
    private static final boolean NOT_USE_SSL = false;
    private static final boolean USE_DATAHUB = true;
    private static final boolean NOT_USE_DATAHUB = false;

    @Test
    public void testGetAddress() {
        String region = "eu";
        InsightOpsClient client = new InsightOpsClient(USE_HTTP_PUT, NOT_USE_SSL, NOT_USE_DATAHUB, "", 0, region);
        String expectedApiServer = String.format(API_TOKEN_SERVER, region);
        assertEquals(client.getAddress(), expectedApiServer, expectedApiServer  + " should be used for HTTP PUT");

        String expectedDataServer = String.format(API_TOKEN_SERVER, region);
        InsightOpsClient client2 = new InsightOpsClient(NOT_USE_HTTP_PUT, NOT_USE_SSL, NOT_USE_DATAHUB, "", 0, region);
        assertEquals(client2.getAddress(), expectedDataServer, expectedDataServer + " should be used for Token TCP");

        InsightOpsClient client3 = new InsightOpsClient(USE_HTTP_PUT, USE_SSL, USE_DATAHUB, "127.0.0.1", 10000, region);
        assertEquals(client3.getAddress(), DATAHUB_IP, "Address 127.0.0.1 should be used over " + expectedApiServer);

        InsightOpsClient client4 = new InsightOpsClient(USE_HTTP_PUT, NOT_USE_SSL, USE_DATAHUB, "127.0.0.1", 10000, region);
        assertEquals(client4.getAddress(), DATAHUB_IP, "Address 127.0.0.1 should be used over " + expectedApiServer);

        InsightOpsClient client5 = new InsightOpsClient(NOT_USE_HTTP_PUT, USE_SSL, USE_DATAHUB, "127.0.0.1", 10000, region);
        assertEquals(client5.getAddress(), DATAHUB_IP, "Address 127.0.0.1 should be used over " + expectedApiServer);

        InsightOpsClient client6 = new InsightOpsClient(NOT_USE_HTTP_PUT, NOT_USE_SSL, USE_DATAHUB, "127.0.0.1", 10000, region);
        assertEquals(client6.getAddress(), DATAHUB_IP, "Address 127.0.0.1 should be used over " + expectedApiServer);

        InsightOpsClient client7 = new InsightOpsClient(NOT_USE_HTTP_PUT, USE_SSL, NOT_USE_DATAHUB, "127.0.0.1", 10000, region);
        assertEquals(client5.getAddress(), DATAHUB_IP, "Address 127.0.0.1 should be used over " + expectedApiServer);

        InsightOpsClient client8 = new InsightOpsClient(NOT_USE_HTTP_PUT, NOT_USE_SSL, NOT_USE_DATAHUB, "127.0.0.1", 10000, region);
        assertEquals(client6.getAddress(), DATAHUB_IP, "Address 127.0.0.1 should be used over " + expectedApiServer);
    }

    @Test
    public void testGetPort() {
        InsightOpsClient client = new InsightOpsClient(USE_HTTP_PUT, USE_SSL, USE_DATAHUB, "127.0.0.1", 10000, "eu");
        assertEquals(client.getPort(), DATAHUB_PORT, "Port 10000 should be used over 443");

        InsightOpsClient client2 = new InsightOpsClient(NOT_USE_HTTP_PUT, NOT_USE_SSL, USE_DATAHUB, "127.0.0.1", 10000, "eu");
        assertEquals(client2.getPort(), DATAHUB_PORT, "Port 10000 should be used over 80");

        InsightOpsClient client3 = new InsightOpsClient(USE_HTTP_PUT, USE_SSL, NOT_USE_DATAHUB, "", 0, "");
        assertEquals(client3.getPort(), IOPS_SSL_PORT, "Port 443 should be used for SSL over HTTP");

        InsightOpsClient client4 = new InsightOpsClient(USE_HTTP_PUT, NOT_USE_SSL, NOT_USE_DATAHUB, "", 0, "");
        assertEquals(client4.getPort(), IOPS_PORT, "Port 80 should be used for HTTP PUT");

        InsightOpsClient client5 = new InsightOpsClient(NOT_USE_HTTP_PUT, USE_SSL, NOT_USE_DATAHUB, "", 0, "");
        assertEquals(client5.getPort(), IOPS_SSL_PORT, "Port 443 should be used for SSL over Token TCP");

        InsightOpsClient client6 = new InsightOpsClient(NOT_USE_HTTP_PUT, NOT_USE_SSL, NOT_USE_DATAHUB, "", 0, "");
        assertEquals(client6.getPort(), IOPS_PORT, "Port 80 should be used for Token TCP");

        InsightOpsClient client7 = new InsightOpsClient(NOT_USE_HTTP_PUT, NOT_USE_SSL, NOT_USE_DATAHUB, "", 10000, "");
        assertEquals(client7.getPort(), 10000, "Port 10000 should be used because specified in the configuration");

        InsightOpsClient client8 = new InsightOpsClient(NOT_USE_HTTP_PUT, USE_SSL, NOT_USE_DATAHUB, "", 10000, "");
        assertEquals(client8.getPort(), 10000, "Port 10000 should be used because specified in the configuration");
    }

}
