/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.common.utils

class JSONData {

    public static final String JSON_REQUEST_EXAMPLE_1 = "{" +
        "\"clientIdentifier\":\"client1\"," +
        "\"hostname\":\"client1.ericsson.net\"," +
        "\"fixedAddress\":\"192.168.1.1/24\"," +
        "\"defaultRouter\":\"192.168.1.10\"" +
        "}"

    public static final String JSON_RESPONSE_EXAMPLE_1 = "{" +
        "\"clientIdentifier\": \"client1\"," +
        "\"hostname\": \"client1.ericsson.net\"," +
        "\"fixedAddress\": \"192.168.1.1/24\"," +
        "\"defaultRouter\": \"192.168.1.10\"" +
        "}"

    public static final String JSON_REQUEST_EXAMPLE_2 = "{" +
        "\"clientIdentifier\":\"client1\"," +
        "\"hostname\":\"client1.ericsson.net\"," +
        "\"fixedAddress\":\"192.168.1.1/24\"," +
        "\"defaultRouter\":\"192.168.1.10\"" +
        "}"

    public static final String JSON_BROKEN_RESPONSE_EXAMPLE_2 = "{" +
        "\"clientIdentifier\": \"client1\"," +
        "\"hostname\": \"client1.ericsson.net\"," +
        "\"fixedAddress\": \"192.168.1.1/24\"," +
        "\"defaultRouter\": \"192.168.1.10\"" +
        "" // <- missing end bracket

    public static final String JSON_RESPONSE_NOT_FOUND_ERROR = "{\n" +
        "\"userMessage\": \"Client does not exist\",\n" +
        "\"internalErrorCode\": 12345,\n" +
        "\"developerMessage\": \"Provide another client clientIdentifier\"\n" +
        "}"

}
