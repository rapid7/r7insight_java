package com.rapid7.net;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.rapid7.Constants.DATA_ENDPOINT_TEMPLATE;

/**
 * Client for sending messages to InsightOPS via HTTP PUT or Token-Based Logging
 * Supports SSL/TLS
 *
 */
public class InsightOpsClient {
    /*
     * Constants
	 */

    /**
     * Port number for HTTP PUT/Token TCP logging on Logentries server.
     */
    private static final int IOPS_PORT = 80;
    /**
     * Port number for SSL HTTP PUT/TLS Token TCP logging on Logentries server.
     */
    private static final int IOPS_SSL_PORT = 443;

    final SSLSocketFactory ssl_factory;
    private boolean ssl_choice;
    private boolean http_choice = false;
    private Socket socket;
    private OutputStream stream;
    private int port;
    private String dataEndpoint;

    public InsightOpsClient(boolean httpPut, boolean ssl, boolean isUsingDataHub, String server, int port, String region) {
        if (isUsingDataHub) {
            ssl_factory = null; // DataHub does not support input over SSL for now,
            this.ssl_choice = false; // so SSL flag is ignored
        } else {
            ssl_factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            ssl_choice = ssl;
            http_choice = httpPut;
        }
        setPort(port, ssl);
        setAddress(server, region);
    }

    private void setPort(int port, boolean ssl_choice){
        if (port > 0) { //use the specified port if provided
            this.port =  port;
        } else {
            this.port = ssl_choice ? IOPS_SSL_PORT : IOPS_PORT;
        }
    }

    private void setAddress(String server, String region){
        if (Utils.isNullOrEmpty(server)) {
            this.dataEndpoint = String.format(DATA_ENDPOINT_TEMPLATE, region);
        } else {
            this.dataEndpoint = server;
        }
    }
    public int getPort() {
        return port;
    }

    public String getAddress() {
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
        } catch (Exception ignored) {}
    }
}
