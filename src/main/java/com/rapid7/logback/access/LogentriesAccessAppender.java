package com.rapid7.logback.access;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
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
 * Logentries appender for logback-access lib.
 *
 * @author Renan Stuchi
 */
public class LogentriesAccessAppender extends LogentriesAppenderBase<IAccessEvent, PatternLayout> {

  /**
   * Default Suffix Pattern
   */
  public static final String DEFAULT_SUFFIX_PATTERN = "%h %l %u [%t] \"%r\" %s %b";

  @Override
  public PatternLayout getPatternLayout() {
    return new PatternLayout();
  }

  @Override
  public String getDefaultSuffixPattern() {
    return DEFAULT_SUFFIX_PATTERN;
  }
}
