package com.rapid7.jul;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Stub of logentries server
 */
public class SocketChannelReceiver {

    private BlockingQueue<String> messagesReceived = new ArrayBlockingQueue<String>(10);
    private final int port;
    private ServerSocketChannel serverSocket;
    private boolean isReady = false;

    private SocketChannelReceiver(int port) {
        this.port = port;
    }

    public static SocketChannelReceiver createAndStartReceiver(int port) throws Exception {
        final SocketChannelReceiver receiver = new SocketChannelReceiver(port);
        Thread receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiver.start();
            }
        });
        receiverThread.start();
        receiver.waitUntilReady();
        return receiver;
    }

    private void start() {
        try {
            init();
            System.out.println("SocketChannelReceiver, waiting for client ...");
            SocketChannel clientSocketChannel = serverSocket.accept();
            System.out.println("Connection Established.");
            listenForMessages(clientSocketChannel);
        } catch (IOException e) {
            System.out.println("Error running SocketChannelReceiver!");
            e.printStackTrace();
        }
    }

    public String pollMessage() throws Exception {
        return messagesReceived.poll(3, TimeUnit.SECONDS);
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (Exception e) {
        }
    }

    private synchronized void init() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        isReady = true;
        notifyAll();
    }

    private synchronized void waitUntilReady() throws InterruptedException {
        if (!isReady) {
            wait(5000);
        }
    }

    private void listenForMessages(SocketChannel clientSocketChannel) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (serverSocket.isOpen()) { // listening for messages
            int messageLength = clientSocketChannel.read(buffer);
            if (messageLength == 0) {
                continue;
            }
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String[] messages = new String(bytes).split("\\r?\\n");
            Collections.addAll(messagesReceived, messages);
            buffer.clear();
        }
    }

}
