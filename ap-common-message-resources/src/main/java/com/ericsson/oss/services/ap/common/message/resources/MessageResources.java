/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.message.resources;

import java.util.Locale;
import java.util.Set;

/**
 * <pre>
 * &#64;Inject
 * MessageResources propertyMessageResources; // Where class that is doing the injection is used and the default message resource file ap_messages.properties is used
 *
 * &#64;Inject
 * &#64;MessageResourceInfo(messageResource = "new_messages.properties")
 * MessageResources propertyMessageResources2; // Where class that is doing the injection is used and an alternative message resource file is used
 *
 * &#64;Inject
 * &#64;@MessageResourceInfo PropertyMessageResources resourceBundle3; // Where class that is doing the injection is used and the default message resource file messages.properties is used
 *
 * &#64;Inject
 * &#64;@MessageResourceInfo(thisClass = xyz.class, messageResource = "ap_messages.properties")
 * MessageResources propertyMessageResources4; // Where a class that does injection is defined and we can define a message resource file
 * </pre>
 */
public interface MessageResources {

    /**
     * Method takes in a key and returns the corresponding value.
     *
     * @param key
     *            the key for the string
     * @return the corresponding value
     */
    String getString(String key);

    /**
     * Method takes in key, and formats it based on the input arguments.
     *
     * @param key
     *            the key for the string
     * @param args
     *            the arguments used to format the string
     * @return the corresponding value formatted with the input arguments
     */
    String format(String key, Object... args);

    /**
     * Method takes in a key returns the corresponding value as an int.
     *
     * @param key
     *            the key for the string
     * @return the corresponding value as an int
     */
    int getInt(String key);

    /**
     * Method gets a key by string and returns the corresponding value as true of false.
     *
     * @param key
     *            the key for the string
     * @return the corresponding value as a boolean
     */
    boolean getBoolean(String key);

    /**
     * Return a value, split using comma as the delimiter, as a String array.
     *
     * @param key
     *            the key for the string
     * @return the corresponding value split using a comma as the delimiter
     */
    String[] getStringArray(String key);

    /**
     * Gets the current locale.
     *
     * @return the current {@link Locale}
     */
    Locale getLocale();

    /**
     * Method takes in key and checks if properties file has the key.
     *
     * @param key
     *            the key to check
     * @return boolean true if the properties file has the key
     */
    boolean containsKey(String key);

    /**
     * Returns the the keys of the properties file as a Set.
     *
     * @return all keys in the properties file
     */
    Set<String> keySet();
}
