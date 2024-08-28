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
package com.ericsson.oss.services.ap.api.workflow;

/**
 * Interface to provide capability information regarding the AP Hardware Replace use case.
 */
public interface HardwareReplaceCapabilty {

    /**
     * Check if the hardware replace capability is supported for a given node type.
     *
     * Unless otherwise specified by the implementing class, returns false.
     *
     * The default implementation should be overridden for node types that support the hardware replace
     * capability.
     *
     * @param nodeType
     *            the nodeType to check
     * @return true if hardware replace is supported by the node type.
     */
    default boolean isSupported(String nodeType) {
        return false;
    }

    /**
     * Check if the hardware replace backup selection capability is supported for a given node type.
     *
     * Unless otherwise specified by the implementing class, returns false.
     *
     * The default implementation should be overridden for node types that support the hardware replace
     * backup selection capability.
     *
     * @param nodeType
     *            the nodeType to check
     * @return true if hardware replace backup selection is supported by the node type.
     */
    default boolean isBackupSelectionSupported(String nodeType) {
        return false;
    }
}
