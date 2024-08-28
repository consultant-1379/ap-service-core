/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.capability;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The service capability model utility.
 */
public enum ServiceCapabilityModel {

    INSTANCE;

    private static final String SERVICE_CAPABILITY_MODEL_PATH = "servicecapability/servicecapability.json";
    private static final String LOAD_MODEL_ERROR_MSG = "Failed to load capabilities. Reason: %s.";
    private static final String READ_CAPABILITY_ERROR_MESSAGE = "Failed to read capabilities for %s. Reason: %s.";
    private final Map<String, List<SecurityCapability>> capabilityModel = loadCapabilityModel();

    /**
     * The base function to get the required capabilities for an use case.
     *
     * @param useCase
     *            the use case
     * @return the list of required capabilities
     */
    public List<SecurityCapability> getRequiredCapabilities(final String useCase) {
        try {
            return capabilityModel.get(useCase);
        } catch (final Exception e) {
            throw new ApApplicationException(String.format(READ_CAPABILITY_ERROR_MESSAGE, useCase, e.getMessage()));
        }
    }

    private Map<String, List<SecurityCapability>> loadCapabilityModel() {
        final Gson gson = new Gson();
        try {
            final InputStream sourceFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(SERVICE_CAPABILITY_MODEL_PATH);
            return gson.fromJson(new InputStreamReader(sourceFile),
                                 new TypeToken<Map<String, List<SecurityCapability>>>() {}.getType());
        } catch (final Exception e) {
            throw new ApApplicationException(String.format(LOAD_MODEL_ERROR_MSG, SERVICE_CAPABILITY_MODEL_PATH));
        }
    }

}
