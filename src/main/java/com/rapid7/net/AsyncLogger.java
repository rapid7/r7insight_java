package com.rapid7.net;

import com.google.common.base.Strings;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * InsightOps Asynchronous Logger for integration with Java logging frameworks.
 * <p>
 * a Rapid7™ service
 * <p>
 * VERSION: 1.2.0
 *
 * @author Viliam Holub
 * @author Mark Lacomber
 */

public final class AsyncLogger {

    /*
     * Constants
     */

    /**
     * Limit on individual log length ie. 2^16
     */
    public static final int LOG_LENGTH_LIMIT = 65536;
    /**
     * Size of the internal event queue.
     */
    private static final int QUEUE_SIZE = 32768;
    /**
     * Limit on recursion for appending long logs to queue
     */
    private static final int RECURSION_LIMIT = 32;
    /**
     * UTF-8 output character set.
     */
    private static final Charset UTF8 = Charset.forName("UTF-8");
    /**
     * ASCII character set used by HTTP.
     */
    private static final Charset ASCII = Charset.forName("US-ASCII");
    /**
     * Minimal delay between attempts to reconnect in milliseconds.
     */
    private static final int MIN_DELAY = 100;
    /**
     * Maximal delay between attempts to reconnect in milliseconds.
     */
    private static final int MAX_DELAY = 10000;
    /**
     * IOPS appender signature - used for debugging messages.
     */
    private static final String IOPS = "IOPS ";
    /**
     * Platform dependent line separator to check for. Supported in Java 1.6+
     */
    private static final String LINE_SEP = System.getProperty("line_separator", "\n");
    /**
     * Error message displayed when invalid API key is detected.
     */
    private static final String INVALID_TOKEN = "\n\nIt appears your LOGENTRIES_TOKEN parameter in log4j.xml is incorrect!\n\n";
    private static final String INVALID_REGION = "\n\nMissing REGION parameter in logger configuration.\n\n";
    /**
     * Key Value for Token Environment Variable.
     */
    private static final String CONFIG_TOKEN = "LOGENTRIES_TOKEN";
    /**
     * Error message displayed when queue overflow occurs
     */
    private static final String QUEUE_OVERFLOW = "\n\nInsightOps Buffer Queue Overflow. Message Dropped!\n\n";
    /**
     * Identifier for this client library
     */
    private static final String LIBRARY_ID = "###J01### - Library initialised";

    /**
     * Reg.ex. that is used to check correctness of HostName if it is defined by user
     */
    private static final Pattern HOSTNAME_REGEX = Pattern.compile("[$/\\\"&+,:;=?#|<>_* \\[\\]]");

    /*
     * Fields
     */

    /**
     * Destination Token.
     */
    private final String token;

    /**
     * Region.
     */
    private final String region;
    /**
     * Account Key.
     */
    private final String key;
    /**
     * Account Log Location.
     */
    private final String location;
    /**
     * HttpPut flag.
     */
    private final boolean httpPut;
    /**
     * SSL/TLS flag.
     */
    private final boolean ssl;
    /**
     * Debug flag.
     */
    private final boolean debug;

    /**
     * UseDataHub flag.
     */
    private final boolean useDataHub;
    /**
     * DataHubAddr - address of the server where DataHub instance resides.
     */
    private final String dataHubAddr;
    /**
     * DataHubPort - port on which DataHub instance waits for messages
     */
    private final int dataHubPort;
    /**
     * LogHostName - switch that determines whether HostName should be appended to the log message
     */
    private final boolean logHostName;
    /**
     * HostName - value, that should be appended to the log message if logHostName is set to true
     */
    private final String hostName;
    /**
     * LogID - user-defined ID string that is appended to the log message if non-empty
     */
    private final String logID;

    /**
     * Indicator if the socket appender has been started.
     */
    private boolean started = false;

    /**
     * Asynchronous socket appender.
     */
    private final SocketAppender appender;
    /**
     * Message queue.
     */
    private final ArrayBlockingQueue<String> queue;

    private final String logMessagePrefix;


    /**
     * Initializes asynchronous logging.
     *
     * @param configuration logger options
     */
    public AsyncLogger(LoggerConfiguration configuration) {

        queue = new ArrayBlockingQueue<String>(QUEUE_SIZE);
        // Fill the queue with an identifier message for first entry sent to server
        queue.offer(LIBRARY_ID);

        this.region = configuration.getRegion();
        this.token = calculateToken(configuration);
        this.key = configuration.getKey();
        this.location = configuration.getLocation();
        this.httpPut = configuration.isHttpPut();
        this.ssl = configuration.isSsl();
        this.debug = configuration.isDebug();
        this.useDataHub = configuration.isUseDataHub();
        this.dataHubAddr = configuration.getDataHubAddr();
        this.dataHubPort = configuration.getDataHubPort();
        this.logHostName = configuration.isLogHostName();
        this.hostName = calculateHostName(configuration);
        this.logID = configuration.getLogID();

        this.logMessagePrefix = buildPrefixMessage();
        appender = new SocketAppender();
    }

    private String calculateToken(LoggerConfiguration configuration) {
        if (!configuration.isHttpPut()
                && (Strings.isNullOrEmpty(configuration.getToken()) || configuration.getToken().equals(CONFIG_TOKEN))) {
            //Check if set in an environment variable, used with PaaS providers
            String envToken = getEnvVar(CONFIG_TOKEN);
            if (envToken.equals("")) {
                dbg(INVALID_TOKEN);
            }
            return envToken;
        }
        return configuration.getToken();
    }

    private String calculateHostName(LoggerConfiguration configuration) {
        if (configuration.isLogHostName()) {
            if (Strings.isNullOrEmpty(configuration.getHostName())) {
                dbg("Host name is not defined by user - trying to obtain it from the environment.");
                try {
                    return InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    // We cannot resolve local host name - so won't use it at all.
                    dbg("Failed to get host name automatically; Host name will not be used in prefix.");
                }
            } else {
                if (!checkIfHostNameValid(configuration.getHostName())) {
                    // User-defined HostName is invalid - e.g. with prohibited characters,
                    // so we'll not use it.
                    dbg("There are some prohibited characters found in the host name defined in the config; Host name will not be used in prefix.");
                    return null;
                }
            }
        }
        return configuration.getHostName();
    }

    /**
     * Returns current token.
     *
     * @return current token
     */
    public String getToken() {
        return token;
    }

    String getLogMessagePrefix() {
        return logMessagePrefix;
    }

    public String getRegion() {
        return region;
    }

    /**
     * Returns current HttpPut flag.
     *
     * @return true if HttpPut is enabled
     */
    public boolean getHttpPut() {
        return this.httpPut;
    }


    /**
     * Gets the ACCOUNT KEY value for HTTP PUT
     *
     * @return key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Gets the LOCATION value for HTTP PUT
     *
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the SSL boolean flag
     *
     * @return ssl
     */
    public boolean getSsl() {
        return this.ssl;
    }


    /**
     * Returns current debug flag.
     *
     * @return true if debugging is enabled
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Gets value of useDataHub flag.
     *
     * @return true if a DataHub instance is used as receiver for log messages.
     */
    public boolean getUseDataHub() {
        return this.useDataHub;
    }

    /**
     * Gets DataHub instance address.
     *
     * @return DataHub address represented as String
     */
    public String getDataHubAddr() {
        return this.dataHubAddr;
    }

    /**
     * Gets port on which DataHub waits for log messages.
     *
     * @return port number.
     */
    public int getDataHubPort() {
        return this.dataHubPort;
    }

    /**
     * Gets value of the switch that determines whether to send HostName alongside with the log message
     *
     * @return logHostName switch value
     */
    public boolean getLogHostName() {
        return this.logHostName;
    }

    /**
     * Gets HostName parameter
     *
     * @return Host name field value
     */
    public String getHostName() {
        return this.hostName;
    }


    /**
     * Gets LogID parameter
     *
     * @return logID field value
     */
    public String getLogID() {
        return this.logID;
    }


    /**
     * Checks that the UUID is valid
     */
    boolean checkValidUUID(String uuid) {
        if ("".equals(uuid))
            return false;
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Try and retrieve environment variable for given key, return empty string if not found
     */

    private String getEnvVar(String key) {
        String envVal = System.getenv(key);
        return envVal != null ? envVal : "";
    }

    /**
     * Checks that key and location are set.
     */
    boolean checkCredentials() {
        if (isNullOrEmpty(region)) {
            dbg(INVALID_REGION);
            return false;
        }
        if (!httpPut) {
            return checkValidUUID(this.getToken());
        } else {
            if (!checkValidUUID(this.getKey()) || isNullOrEmpty(location))
                return false;
        }
        return true;
    }

    /**
     * Builds the prefix message.
     */
    private String buildPrefixMessage() {
        StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(logID)) {
            sb.append(logID).append(" "); // Append LogID and separator between logID and the rest part of the message.
        }
        if (logHostName && !Strings.isNullOrEmpty(hostName)) {
            sb.append("HostName=").append(hostName).append(" ");
        }
        return sb.toString();
    }

    /**
     * Checks whether given host name is valid (e.g. does not contain any prohibited characters)
     *
     * @param hostName - string containing host name
     */
    boolean checkIfHostNameValid(String hostName) {
        return !HOSTNAME_REGEX.matcher(hostName).find();
    }

    /**
     * Adds the data to internal queue to be sent over the network.
     * <p>
     * It does not block. If the queue is full, it removes latest event first to
     * make space.
     *
     * @param line line to append
     */
    public void addLineToQueue(String line) {
        addLineToQueue(line, RECURSION_LIMIT);
    }

    private void addLineToQueue(String line, int limit) {
        if (limit == 0) {
            dbg("Message longer than " + RECURSION_LIMIT * LOG_LENGTH_LIMIT);
            return;
        }

        //// Check credentials only if logs are sent to Insight OPS directly.
        // Check that we have all parameters set and socket appender running.
        // If DataHub mode is used then credentials check is ignored.
        if (!this.started && (useDataHub || this.checkCredentials())) {
            dbg("Starting InsightOps asynchronous socket appender");
            appender.start();
            started = true;
        }

        dbg("Queueing " + line);

        // If individual string is too long add it to the queue recursively as sub-strings
        if (line.length() > LOG_LENGTH_LIMIT) {
            if (!queue.offer(line.substring(0, LOG_LENGTH_LIMIT))) {
                queue.poll();
                if (!queue.offer(line.substring(0, LOG_LENGTH_LIMIT)))
                    dbg(QUEUE_OVERFLOW);
            }
            addLineToQueue(line.substring(LOG_LENGTH_LIMIT, line.length()), limit - 1);

        } else {
            // Try to append data to queue
            if (!queue.offer(line)) {
                queue.poll();
                if (!queue.offer(line))
                    dbg(QUEUE_OVERFLOW);
            }
        }
    }

    /**
     * Closes all connections to InsightOps.
     */
    public void close() {
        appender.interrupt();
        started = false;
        dbg("Closing InsightOps asynchronous socket appender");
    }

    /**
     * Prints the message given. Used for internal debugging.
     *
     * @param msg message to display
     */
    void dbg(String msg) {
        if (debug) {
            if (!msg.endsWith(LINE_SEP)) {
                System.err.println(IOPS + msg);
            } else {
                System.err.print(IOPS + msg);
            }
        }
    }


    /**
     * Asynchronous over the socket appender.
     *
     * @author Viliam Holub
     */
    class SocketAppender extends Thread {
        /**
         * Random number generator for delays between reconnection attempts.
         */
        final Random random = new Random();
        /**
         * Logentries Client for connecting to InsightOPS via HTTP or TCP.
         */
        InsightOpsClient iopsClient;

        /**
         * Initializes the socket appender.
         */
        SocketAppender() {
            super("InsightOps Socket appender");
            // Don't block shut down
            setDaemon(true);
        }

        /**
         * Opens connection to InsightOps.
         *
         * @throws IOException
         */
        void openConnection() throws IOException {
            try {
                if (this.iopsClient == null) {
                    this.iopsClient = new InsightOpsClient(httpPut, ssl, useDataHub, dataHubAddr, dataHubPort, region);
                }
                this.iopsClient.connect();

                if (httpPut) {
                    final String f = "PUT /%s/hosts/%s/?realtime=1 HTTP/1.1\r\n\r\n";
                    final String header = String.format(f, key, location);
                    byte[] temp = header.getBytes(ASCII);
                    this.iopsClient.write(temp, 0, temp.length);
                }

            } catch (Exception e) {

            }
        }

        /**
         * Tries to opens connection to InsightOps until it succeeds.
         *
         * @throws InterruptedException
         */
        void reopenConnection() throws InterruptedException {
            // Close the previous connection
            closeConnection();

            // Try to open the connection until we get through
            int root_delay = MIN_DELAY;
            while (true) {
                try {
                    openConnection();

                    // Success, leave
                    return;
                } catch (IOException e) {
                    // Get information if in debug mode
                    if (debug) {
                        dbg("Unable to connect to InsightOps");
                        e.printStackTrace();
                    }
                }

                // Wait between connection attempts
                root_delay *= 2;
                if (root_delay > MAX_DELAY)
                    root_delay = MAX_DELAY;
                int wait_for = root_delay + random.nextInt(root_delay);
                dbg("Waiting for " + wait_for + "ms");
                Thread.sleep(wait_for);
            }
        }

        /**
         * Closes the connection. Ignores errors.
         */
        void closeConnection() {
            if (this.iopsClient != null)
                this.iopsClient.close();
        }


        /**
         * Initializes the connection and starts to log.
         */
        @Override
        public void run() {
            try {
                // Open connection
                reopenConnection();

                // Use StringBuilder here because if use just overloaded
                // + operator it may give much more work for allocator and GC.
                StringBuilder finalDataBuilder = new StringBuilder("");

                // Send data in queue
                while (true) {
                    // Take data from queue
                    String data = queue.take();

                    // Replace platform-independent carriage return with unicode line separator character to format multi-line events nicely in Logentries UI
                    data = data.replace(LINE_SEP, "\u2028");

                    finalDataBuilder.setLength(0); // Clear the buffer to be re-used - it may be faster than re-allocating space for new String instances.

                    // If we're neither sending to DataHub nor using HTTP PUT
                    // then append the token to the start of the message.
                    if (!httpPut && !useDataHub) {
                        finalDataBuilder.append(token);
                    }

                    // If message prefix (LogID + HostName) is not empty
                    // then add it to the message.
                    if (!Strings.isNullOrEmpty(logMessagePrefix)) {
                        finalDataBuilder.append(logMessagePrefix);
                    }

                    // Append the event data
                    finalDataBuilder.append(data).append('\n');

                    // Get bytes of final event
                    byte[] finalLine = finalDataBuilder.toString().getBytes(UTF8);

                    // Send data, reconnect if needed
                    while (true) {
                        try {
                            this.iopsClient.write(finalLine, 0, finalLine.length);
                        } catch (IOException e) {
                            // Reopen the lost connection
                            reopenConnection();
                            continue;
                        }
                        break;
                    }
                }
            } catch (InterruptedException e) {
                // We got interrupted, stop
                dbg("Asynchronous socket writer interrupted");
                dbg("Queue had " + queue.size() + " lines left in it");
            }

            closeConnection();
        }
    }
}
