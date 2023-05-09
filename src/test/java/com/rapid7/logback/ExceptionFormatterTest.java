package com.rapid7.logback;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionFormatterTest {

    @Test
    public void testTopLevelTrace() {
        IThrowableProxy error = Mockito.mock(IThrowableProxy.class);

        StackTraceElementProxy l1 = Mockito.mock(StackTraceElementProxy.class);
        Mockito.when(l1.getSTEAsString()).thenReturn("trace level 1");

        StackTraceElementProxy[] topLevel = new StackTraceElementProxy[]{l1};

        Mockito.when(error.getStackTraceElementProxyArray()).thenReturn(topLevel);
        Mockito.when(error.getClassName()).thenReturn("com.foo.SomeClass");
        Mockito.when(error.getMessage()).thenReturn("err!");

        String trace = ExceptionFormatter.formatException(error);
        assertEquals(trace, "com.foo.SomeClass: err!" +
                ExceptionFormatter.DELIMITER + ExceptionFormatter.TAB +
                "trace level 1" + ExceptionFormatter.DELIMITER);
    }
}
