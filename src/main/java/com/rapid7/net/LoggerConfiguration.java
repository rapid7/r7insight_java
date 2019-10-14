package com.rapid7.net;

/**
 * Factory Data.
 */
public class LoggerConfiguration {

    /**
     * Destination Token.
     */
    private String token;
    /**
     * Destination Region.
     */
    private String region;
    /**
     * Account Key.
     */
    private String key;
    /**
     * Account Log Location.
     */
    private String location;
    /**
     * HttpPut flag.
     */
    private boolean httpPut;
    /**
     * SSL/TLS flag.
     */
    private boolean ssl;
    /**
     * Debug flag.
     */
    private boolean debug;
    /**
     * UseDataHub flag.
     */
    private boolean useDataHub;
    /**
     * DataHubAddr - address of the server where DataHub instance resides.
     */
    private String dataHubAddr;
    /**
     * DataHubPort - port on which DataHub instance waits for messages.
     */
    private int dataHubPort;
    /**
     * LogHostName - switch that determines whether HostName should be appended to the log message.
     */
    private boolean logHostName;
    /**
     * HostName - value, that should be appended to the log message if logHostName is set to true.
     */
    private String hostName;
    /**
     * LogID - user-defined ID string that is appended to the log message if non-empty.
     */
    private String logID;

    private LoggerConfiguration() {
    }

    public String getToken() {
        return token;
    }

    public String getKey() {
        return key;
    }

    public String getLocation() {
        return location;
    }

    public boolean isHttpPut() {
        return httpPut;
    }

    public boolean isSsl() {
        return ssl;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isUseDataHub() {
        return useDataHub;
    }

    public String getDataHubAddr() {
        return dataHubAddr;
    }

    public int getDataHubPort() {
        return dataHubPort;
    }

    public boolean isLogHostName() {
        return logHostName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getLogID() {
        return logID;
    }

    public String getRegion() {
        return region;
    }

    public static class Builder {
        private String token;
        private String region;
        private String key;
        private String location = "";
        private boolean httpPut;
        private boolean ssl = true;
        private boolean debug;
        private boolean useDataHub;
        private String dataHubAddr;
        private int dataHubPort;
        private boolean logHostName;
        private String hostName;
        private String logID;

        public Builder useToken(String token) {
            this.token = token;
            return this;
        }

        public Builder inRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder useAccountKey(String key) {
            this.key = key;
            return this;
        }

        public Builder useHttpPut(boolean httpPut) {
            this.httpPut = httpPut;
            return this;
        }

        public Builder httpPutLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder runInDebugMode(boolean debugMode) {
            this.debug = debugMode;
            return this;
        }

        public Builder useSSL(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder useDataHub(boolean useDataHub) {
            this.useDataHub = useDataHub;
            return this;
        }

        public Builder toServerAddress(String address) {
            this.dataHubAddr = address;
            return this;
        }

        public Builder toServerPort(int port) {
            this.dataHubPort = port;
            return this;
        }

        public Builder logHostNameAsPrefix(boolean logHostName) {
            this.logHostName = logHostName;
            return this;
        }

        public Builder useAsHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder setLogIdPrefix(String logID) {
            this.logID = logID;
            return this;
        }

        public LoggerConfiguration build() {
            LoggerConfiguration configuration = new LoggerConfiguration();
            configuration.token = token;
            configuration.region = region;
            configuration.key = key;
            configuration.location = location;
            configuration.httpPut = httpPut;
            configuration.ssl = ssl;
            configuration.debug = debug;
            configuration.useDataHub = useDataHub;
            configuration.dataHubAddr = dataHubAddr;
            configuration.dataHubPort = dataHubPort;
            configuration.logHostName = logHostName;
            configuration.hostName = hostName;
            configuration.logID = logID;
            return configuration;
        }
    }
}
