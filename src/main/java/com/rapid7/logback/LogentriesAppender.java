package com.rapid7.logback;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.net.SyslogConstants;
import com.rapid7.logback.LogentriesAppenderBase;
import com.rapid7.net.AsyncLogger;
import com.rapid7.net.LoggerConfiguration;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Logentries appender for logback-classic events.
 *
 * @author Renan Stuchi
 */
public class LogentriesAppender extends LogentriesAppenderBase<ILoggingEvent, PatternLayout> {

    /**
     * Default Suffix Pattern
     */
    public static final String DEFAULT_SUFFIX_PATTERN = "[%thread] %logger %msg";

    @Override
    public PatternLayout getPatternLayout() {
        return new PatternLayout();
    }

    @Override
    public String getDefaultSuffixPattern() {
        return DEFAULT_SUFFIX_PATTERN;
    }
}
