package com.rapid7.net;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.rapid7.Constants.HTTP_ENDPOINT_TEMPLATE;
import static com.rapid7.Constants.DATA_ENDPOINT_TEMPLATE;

/**
 * Client for sending messages to Logentries via HTTP PUT or Token-Based Logging
 * Supports SSL/TLS
 *
 * @author Mark Lacomber
 */
public class LogentriesClient {
    /*
     * Constants
	 */

    /**
     * Port number for HTTP PUT/Token TCP logging on Logentries server.
     */
    private static final int LE_PORT = 80;
    /**
     * Port number for SSL HTTP PUT/TLS Token TCP logging on Logentries server.
     */
    private static final int LE_SSL_PORT = 443;

    final SSLSocketFactory ssl_factory;
    private boolean ssl_choice = false;
    private boolean http_choice = false;
    private Socket socket;
    private OutputStream stream;
    private int dataHubPort = LE_PORT;
    private boolean useDataHub = false;
    private String dataEndpoint;
    private String httpEndpoint;
    private String dataHubServer = dataEndpoint;

    public LogentriesClient(boolean httpPut, boolean ssl, boolean isUsingDataHub, String server, int port, String region) {
        if (isUsingDataHub) {
            ssl_factory = null; // DataHub does not support input over SSL for now,
            this.ssl_choice = false; // so SSL flag is ignored
            useDataHub = true;
            dataHubServer = server;
            dataHubPort = port;
        } else {
            ssl_factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            ssl_choice = ssl;
            http_choice = httpPut;
        }
        dataEndpoint = String.format(DATA_ENDPOINT_TEMPLATE, region);
        httpEndpoint = String.format(HTTP_ENDPOINT_TEMPLATE, region);
    }

    public int getPort() {
        if (useDataHub) {
            return dataHubPort;
        } else if (ssl_choice) {
            return LE_SSL_PORT;
        }

        return LE_PORT;
    }

    public String getAddress() {
        if (useDataHub) {
            return dataHubServer;
        } else if (http_choice) {
            return httpEndpoint;
        }

        return dataEndpoint;
    }

    public void connect() throws IOException {
        if (ssl_choice) {
            if (http_choice) {
                SSLSocket s = (SSLSocket) ssl_factory.createSocket(getAddress(), getPort());
                s.setTcpNoDelay(true);
                s.startHandshake();
                socket = s;
            } else {
                socket = SSLSocketFactory.getDefault().createSocket(getAddress(), getPort());
            }
        } else {
            socket = new Socket(getAddress(), getPort());
        }

        this.stream = socket.getOutputStream();
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        if (this.stream == null) {
            throw new IOException();
        }
        this.stream.write(buffer, offset, length);
        this.stream.flush();
    }

    public void close() {
        try {
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {

        }
    }
}
