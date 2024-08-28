/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.message.resources;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.Set;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * Handler for {@link MessageResources}. Primary purpose is to injection of a {@link PropertyResourceBundle} directly into a class.
 */
public class PropertyMessageResources implements MessageResources {

    private final PropertyResourceBundle resourceBundle;

    /**
     * Use the class that is injecting and a String to construct an {@link InputStream} that can be used to load a {@link PropertyResourceBundle}.
     *
     * @param classToInject
     *            the class to inject
     * @param propertiesFile
     *            filePath for the properties file to load
     * @throws IOException
     *             thrown if there is an error loading the properties file
     */
    public PropertyMessageResources(final Class<?> classToInject, final String propertiesFile) throws IOException {
        final Resource resource = Resources.getClasspathResource(propertiesFile);
        if (!resource.exists()) {
            throw new IllegalArgumentException(
                    String.format("The resource %s does not exist on the classpath for class %s", propertiesFile, classToInject));
        }

        try (final InputStream inStream = resource.getInputStream()) {
            resourceBundle = new PropertyResourceBundle(inStream);
        }
    }

    @Override
    public String getString(final String key) {
        return resourceBundle.getString(key);
    }

    @Override
    public String format(final String key, final Object... args) {
        String text = resourceBundle.getString(key);
        if (args.length > 0) {
            text = MessageFormat.format(text, args);
        }
        return text;
    }

    @Override
    public int getInt(final String key) {
        final String resultString = resourceBundle.getString(key);
        return Integer.parseInt(resultString);
    }

    @Override
    public boolean getBoolean(final String key) {
        final String resultString = resourceBundle.getString(key);
        return Boolean.parseBoolean(resultString);
    }

    @Override
    public final String[] getStringArray(final String key) {
        return resourceBundle.getString(key).split(",");
    }

    @Override
    public Locale getLocale() {
        return resourceBundle.getLocale();
    }

    @Override
    public boolean containsKey(final String key) {
        return resourceBundle.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return resourceBundle.keySet();
    }
}
