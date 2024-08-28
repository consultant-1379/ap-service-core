/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.shm.util;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;

public class NodeMoHelper {

    @Inject
    private DpsOperations dpsOperations;

    private static final String NE_PRODUCT_VERSION = "neProductVersion";
    private static final String REVISION = "revision";
    private static final String IDENTITY = "identity";

    /**
     * Retrieves software version from MO.
     *
     * @param nodeName
     *            the name of the AP node
     * @return software version
     */
    public String getSoftwareVersionFromMO(final String nodeName) {
        final ManagedObject networkElementMo = dpsOperations.getDataPersistenceService().getLiveBucket()
                .findMoByFdn("NetworkElement=" + nodeName);
        final Object neProductVersionAttribute = networkElementMo.getAttribute(NE_PRODUCT_VERSION);

        if (!isValidListValue(neProductVersionAttribute)) {
            throw new ApServiceException(String.format("Attribute Value is not properly defined in MO: %1s , Attribute: %2s ",
                    "NetworkElement=" + nodeName, NE_PRODUCT_VERSION));
        }

        final List<Object> neProductVersionList = (List<Object>) neProductVersionAttribute;
        final Map<String, Object> neProductVersionMap = (Map<String, Object>) neProductVersionList.get(0);
        if (!isValidMapValue(neProductVersionMap)) {
            throw new ApServiceException(String.format("Software Upgrade Package is not properly defined in MO: %1s , Attribute: %2s ",
                    "NetworkElement=" + nodeName, NE_PRODUCT_VERSION));
        }
        final String identity = (String) neProductVersionMap.get(IDENTITY);
        final String productRevision = (String) neProductVersionMap.get(REVISION);
        return identity + "_" + productRevision;
    }

    private static boolean isValidListValue(final Object attributeValue) {
        if (attributeValue != null && isList(attributeValue)) {
            return isValidList((List<Object>) attributeValue);
        }
        return false;
    }

    private static boolean isValidList(final List<Object> attributeValue) {
        return !attributeValue.isEmpty();
    }

    private static boolean isValidMapValue(final Object attributeValue) {
        if (attributeValue != null && isMap(attributeValue)) {
            return isValidMap((Map<String, Object>) attributeValue);
        }
        return false;
    }

    private static boolean isMap(final Object attributeValue) {
        return Map.class.isAssignableFrom(attributeValue.getClass());
    }

    private static boolean isList(final Object attributeValue) {
        return List.class.isAssignableFrom(attributeValue.getClass());
    }

    private static boolean isValidMap(final Map<String, Object> attributeValue) {
        return !attributeValue.isEmpty();
    }
}
