[Please see our documentation page for usage information](https://insightops.help.rapid7.com/docs/libraries)
-------

Logging To InsightOps from Java
==============================

InsightOps currently supports logging from Java using the following logging libraries:

* [Log4J2](https://insightops.help.rapid7.com/docs/log4j-log4j2)
* [Logback](https://insightops.help.rapid7.com/docs/logback)
* [Java Util Logging](https://insightops.help.rapid7.com/docs/java-util-logging)


-------

Maximum Log Length
==================

Currently logs which exceed 65536 characters in length, including any patterns and timestamps you may include, will be split and sent as multiple logs.

-------

Configure Java Util Logging with multiple handlers
==================
This library allows you to set up different loggers (java.util.logging.Logger) each of them with a different 
Logentries handler configuration (e.g. to support different tokens). 

1. Add to logging.properties the following props:
    - Log configuration class:
    ```
    config=com.rapid7.jul.MultipleLogentriesHandlerConfig
    ```

    - List all the handler names you need:
    ```
    logentries.handler.names=logger1,logger2
    ```

    - Configure each handler using the handler name specified before as prefix:

    ```
    logger1.com.rapid7.jul.LogentriesHandler.token=4ff1cb0a-beea-4616-b647-1c113de8e7bb
    logger1.com.rapid7.jul.LogentriesHandler.region=eu
    logger1.com.rapid7.jul.LogentriesHandler.formatter=java.util.logging.SimpleFormatter
    
    logger2.com.rapid7.jul.LogentriesHandler.token=a70d9089-576c-4668-9641-14995d493a62
    logger2.com.rapid7.jul.LogentriesHandler.region=eu
    logger2.com.rapid7.jul.LogentriesHandler.formatter=java.util.logging.SimpleFormatter
    ```

2. Get the LogManager and configure it with the property file:
    ```
    LogManager logManager = LogManager.getLogManager();
    InputStream is = new FileInputStream("src/main/resources/logging.properties");
    logManager.readConfiguration(is); 
    ```

3.  Get the loggers:
    ```
    Logger logger1 = Logger.getLogger("logger1");
    Logger logger2 = Logger.getLogger("logger2");
    ```
