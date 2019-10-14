package com.rapid7.util;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Stub of InsightOPS server
 */
public class SocketChannelReceiver {

    private BlockingQueue<String> messagesReceived = new ArrayBlockingQueue<String>(10);
    private final int port;
    private ServerSocket serverSocket;
    private boolean isReady = false;
    private static final char[] SSL_CERTIFICATE_PASSWORD = "keypassword".toCharArray();

    private SocketChannelReceiver(int port) {
        this.port = port;
    }

    public static SocketChannelReceiver createAndStartReceiver(int port, final boolean tlsMode) throws Exception {
        final SocketChannelReceiver receiver = new SocketChannelReceiver(port);
        Thread receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiver.start(tlsMode);
            }
        });
        receiverThread.start();
        receiver.waitUntilReady();
        return receiver;
    }

    private void start(boolean tlsMode) {
        try {
            init(tlsMode);
            System.out.println("SocketChannelReceiver, waiting for client ...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection Established.");
            listenForMessages(socket);
        } catch (Exception e) {
            System.out.println("Error running SocketChannelReceiver!");
            e.printStackTrace();
        }
    }

    public String pollMessage() throws Exception {
        String message = messagesReceived.poll(3, TimeUnit.SECONDS);
        //clean the message from spaces and end line
        return message.replace("\u2028", "").trim();
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("Error closing server socket!");
            e.printStackTrace();
        }
    }

    private synchronized void init(boolean tls) throws Exception {
        if (tls) {
            SSLContext sslContext = initSSlContext();
            serverSocket = sslContext
                    .getServerSocketFactory().createServerSocket(port);
        } else {
            serverSocket = ServerSocketChannel.open().socket();
            serverSocket.bind(new InetSocketAddress(port));
        }
        isReady = true;
        notifyAll();
    }

    private SSLContext initSSlContext() throws Exception {
        // Configure ssl context with unit test key store to support SSL/TLS connections
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksIs = getClass()
                .getClassLoader().getResourceAsStream("unit_test_key_store.jks");
        try {
            ks.load(ksIs, SSL_CERTIFICATE_PASSWORD);
        } finally {
            if (ksIs != null) {
                ksIs.close();
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, SSL_CERTIFICATE_PASSWORD);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }


    private synchronized void waitUntilReady() throws InterruptedException {
        if (!isReady) {
            wait(5000);
        }
    }

    private void listenForMessages(Socket socket) throws IOException {
        byte[] buffer = new byte[1024];
        while (!serverSocket.isClosed()) { // listening for messages
            int messageLength = socket.getInputStream().read(buffer);
            if (messageLength == -1) {
                continue;
            }
            String[] messages = new String(Arrays.copyOf(buffer, messageLength)).split("\\r?\\n");
            Collections.addAll(messagesReceived, messages);
        }
    }

}
