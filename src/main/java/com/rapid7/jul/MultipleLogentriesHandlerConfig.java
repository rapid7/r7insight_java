package com.rapid7.jul;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <code>MultipleLogentriesHandlerConfig</code>: Configuration class to set up multiple Logentries Handlers with different token or host.
 */
public class MultipleLogentriesHandlerConfig {
    private static final String LOGENTRIES_HANDLER_NAMES_PROPERTY_KEY = "logentries.handler.names";

    public MultipleLogentriesHandlerConfig() {
        String handlerNamesPropertyValue = LogManager.getLogManager().getProperty(LOGENTRIES_HANDLER_NAMES_PROPERTY_KEY);
        if (handlerNamesPropertyValue != null) {
            String[] handlerNames = handlerNamesPropertyValue.split(",");
            for (String handlerName : handlerNames) {
                Logger logger = Logger.getLogger(handlerName);
                logger.addHandler(new LogentriesHandler(handlerName));
                logger.setUseParentHandlers(false);
            }
        }
    }
}
