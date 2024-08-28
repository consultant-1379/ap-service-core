/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Container class which loads the 'ap_ui_messages.properties' file from the file system, and loads it into a {@link Properties} object.
 * The values can then be extracted to send responses back to the UI.
 */
public class ApUiMessages {

    private static final String FILE_PATH = "/ap_ui_messages.properties";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Properties singleton instance. This will avoid doing I/O operations
     * every time getProperties is called.
     */
    private Properties properties;

    /**
     * Get a {@link String} from the {@link Properties}.
     *
     * @param propertyName the name of the property to retrieve
     * @return the property value
     */
    public String get(final String propertyName) {
        final Properties apMessages = getProperties();
        return apMessages.getProperty(propertyName);
    }

    /**
     * Get a {@link String} from the {@link Properties}.
     * <p>
     * Uses {@link MessageFormat #format(String, Object...)} to build the output.
     *
     * @param propertyName the name of the property to retrieve
     * @param formatArguments additional arguments to be formatted with the property value
     * @return the property value with additional arguments
     */
    public String format(final String propertyName, final Object... formatArguments) {
        final String message = get(propertyName);
        return MessageFormat.format(message, formatArguments);
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (final InputStream stream = ApUiMessages.class.getResourceAsStream(FILE_PATH)) {
                properties.load(stream);
            } catch (final Exception e) {
                logger.warn("Error loading AP UI properties file from {}", FILE_PATH, e);

                properties = null;
                return new Properties();
            }
        }
        return properties;
    }

}
