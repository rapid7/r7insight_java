package com.rapid7.logback;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.net.SyslogConstants;
import com.rapid7.net.AsyncLogger;
import com.rapid7.net.LoggerConfiguration;

import java.io.UnsupportedEncodingException;

/**
 * Logentries appender for logback.
 *
 * @author Mark Lacomber
 * @author Ben McCann
 * @author Chris Mowforth
 */
public class LogentriesAppender extends AppenderBase<ILoggingEvent> {

	/**
	 * Default Suffix Pattern
	 */
	public static final String DEFAULT_SUFFIX_PATTERN = "[%thread] %logger %msg";
	/**
	 * Asynchronous Background logger
	 */
	AsyncLogger iopsAsync;

	private final LoggerConfiguration.Builder configurationBuilder;
	/**
	 * Layout
	 */
	private Layout<ILoggingEvent> layout;

	private Encoder<ILoggingEvent> encoder;
	/**
	 * Facility String
	 */
	private String facilityStr;
	private String suffixPattern;

	/**
	 * Creates a new Logentries appender.
	 */
	public LogentriesAppender() {
		configurationBuilder = new LoggerConfiguration.Builder();
	}


	/*
	 * Public methods to send logback parameters to AsyncLogger
	 */

	/**
	 * Sets the token.
	 *
	 * @param token Insight OPS token
	 */
	public void setToken(String token) {
		this.configurationBuilder.useToken(token);
	}

	/**
	 * Sets the region.
	 *
	 * @param region region to send the log to (e.g. eu or us)
	 */
	public void setRegion(String region) {
		this.configurationBuilder.inRegion(region);
	}

	/**
	 * Sets the HTTP PUTflag. <p>Send logs via HTTP PUT instead of default Token
	 * TCP.</p>
	 *
	 * @param httpPut true to use HTTP PUT API
	 */
	public void setHttpPut(boolean httpPut) {
		this.configurationBuilder.useHttpPut(httpPut);
	}

	/**
	 * Sets the ACCOUNT KEY value for HTTP PUT.
	 *
	 * @param accountKey account key value (for http put only)
	 */
	public void setKey(String accountKey) {
		this.configurationBuilder.useAccountKey(accountKey);
	}

	/**
	 * Sets the LOCATION value for HTTP PUT.
	 *
	 * @param logLocation location on server (for http put only)
	 */
	public void setLocation(String logLocation) {
		this.configurationBuilder.httpPutLocation(logLocation);
	}

	/**
	 * Sets the SSL boolean flag
	 *
	 * @param ssl true to send logs encrypted over ssl/tls
	 */
	public void setSsl(boolean ssl) {
		this.configurationBuilder.useSSL(ssl);
	}

	/**
	 * Sets the debug flag.
	 * <p>Appender in debug mode will print error messages on error console.</p>
	 *
	 * @param debug debug flag to set
	 */
	public void setDebug(boolean debug) {
		this.configurationBuilder.runInDebugMode(debug);
	}

	/**
	 * Sets the flag which determines if DataHub instance is used instead of Logentries service.
	 *
	 * @param useDataHub set to true to send log messaged to a DataHub instance.
	 */
	public void setIsUsingDataHub(boolean useDataHub) {
		this.configurationBuilder.useDataHub(useDataHub);
	}

	/**
	 * Sets the address where DataHub server resides.
	 *
	 * @param dataHubAddr address like "127.0.0.1"
	 */
	public void setDataHubAddr(String dataHubAddr) {
		this.configurationBuilder.toServerAddress(dataHubAddr);
	}

	/**
	 * Sets the port number on which DataHub instance waits for log messages.
	 *
	 * @param dataHubPort data hub port number
	 */
	public void setDataHubPort(int dataHubPort) {
		this.configurationBuilder.toServerPort(dataHubPort);
	}

	/**
	 * Determines whether to send HostName alongside with the log message
	 *
	 * @param logHostName true to add server host name as log prefix
	 */
	public void setLogHostName(boolean logHostName) {
		this.configurationBuilder.logHostNameAsPrefix(logHostName);
	}

	/**
	 * Sets the HostName from the configuration
	 *
	 * @param hostName host name value
	 */
	public void setHostName(String hostName) {
		this.configurationBuilder.useAsHostName(hostName);
	}

	/**
	 * Sets LogID parameter from the configuration
	 *
	 * @param logID log prefix
	 */
	public void setLogID(String logID) {
		this.configurationBuilder.setLogIdPrefix(logID);
	}

	/**
	 * Sets the encoder for this appender
	 *
	 * @param encoder Logback Encoder
	 */
	public void setEncoder(Encoder<ILoggingEvent> encoder) {
		this.encoder = encoder;
	}

	@Override
	public void start() {
		if (encoder == null) {
			layout = layout == null ? buildLayout() : layout;
			LayoutWrappingEncoder lwe = new LayoutWrappingEncoder<ILoggingEvent>();
			lwe.setLayout(buildLayout());
			lwe.setContext(getContext());
			encoder = lwe;
		}
		this.iopsAsync = new AsyncLogger(configurationBuilder.build());
		super.start();
	}

	String getPrefixPattern() {
		return "%syslogStart{" + getFacility() + "}%nopex";
	}

	/**
	 * Returns the string value of the <b>Facility</b> option.
	 * <p>
	 * See {@link #setFacility} for the set of allowed values.
	 * @return facility name
	 */
	public String getFacility() {
		return facilityStr;
	}

	/**
	 * The <b>Facility</b> option must be set one of the strings KERN, USER,
	 * MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, NTP,
	 * AUDIT, ALERT, CLOCK, LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5,
	 * LOCAL6, LOCAL7. Case is not important.
	 * <p>See {@link SyslogConstants} and RFC 3164 for more information about
	 * the <b>Facility</b> option.
	 *
	 * @param facilityStr facility name
	 */
	public void setFacility(String facilityStr) {
		if (facilityStr != null) {
			facilityStr = facilityStr.trim();
		}
		this.facilityStr = facilityStr;
	}

	public Layout<ILoggingEvent> getLayout() {
		return layout;
	}

	/**
	 * Sets the layout for the Appender
	 *
	 * @param layout logback layout
	 */
	public void setLayout(Layout<ILoggingEvent> layout) {
		this.layout = layout;
	}

	/**
	 * Implements AppenderSkeleton Append method, handles time and format
	 *
	 * @param event event to log
	 */
	@Override
	protected void append(ILoggingEvent event) {
		// Render the event according to layout
		byte[] encodedEvent = encoder.encode(event);
		String formattedEvent;
		try {
			formattedEvent = new String(encodedEvent, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			formattedEvent = new String(encodedEvent);
		}
		// Prepare to be queued
		this.iopsAsync.addLineToQueue(formattedEvent);
	}

	/**
	 * Closes all connections to Logentries
	 */
	@Override
	public void stop() {
		super.stop();
		this.iopsAsync.close();
	}

	public Layout<ILoggingEvent> buildLayout() {
		PatternLayout l = new PatternLayout();
		l.getInstanceConverterMap().put("syslogStart", SyslogStartConverter.class.getName());
		if (suffixPattern == null) {
			suffixPattern = DEFAULT_SUFFIX_PATTERN;
		}
		l.setPattern(getPrefixPattern() + suffixPattern);
		l.setContext(getContext());
		l.start();
		return l;
	}

	/**
	 * See @link #setSuffixPattern(String).
	 *
	 * @return suffix pattern
	 */
	public String getSuffixPattern() {
		return suffixPattern;
	}

	/**
	 * The <b>suffixPattern</b> option specifies the format of the
	 * non-standardized part of the message sent to the syslog server.
	 *
	 * @param suffixPattern suffix pattern
	 */
	public void setSuffixPattern(String suffixPattern) {
		this.suffixPattern = suffixPattern;
	}
}
