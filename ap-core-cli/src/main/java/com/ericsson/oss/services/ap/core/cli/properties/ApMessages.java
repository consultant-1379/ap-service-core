/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.properties;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class which loads the 'ap_messages.properties' file from the file system, and loads it into a {@link Properties} object. The values can
 * then be extracted to send responses back to the CLI.
 */
public class ApMessages {

    private static final String FILE_PATH = "/ap_messages.properties";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Get a {@link String} from the {@link Properties}.
     *
     * @param propertyName
     *            the name of the property to retrieve
     * @return the property value
     */
    public String get(final String propertyName) {
        final Properties apMessages = getProperties();
        return apMessages.getProperty(propertyName);
    }

    /**
     * Get a {@link String} from the {@link Properties}.
     * <p>
     * Uses {@link MessageFormat} #format(String, Object...) to build the output.
     *
     * @param propertyName
     *            the name of the property to retrieve
     * @param formatArguments
     *            additional arguments to be formatted with the property value
     * @return the property value with additional arguments
     */
    public String format(final String propertyName, final Object... formatArguments) {
        final String message = get(propertyName);
        return MessageFormat.format(message, formatArguments);
    }

    private Properties getProperties() {
        final Properties properties = new Properties();
        try (final InputStream stream = ApMessages.class.getResourceAsStream(FILE_PATH)) {
            properties.load(stream);
        } catch (final Exception e) {
            logger.warn("Error loading AP properties file from {}", FILE_PATH, e);
        }
        return properties;
    }
}
