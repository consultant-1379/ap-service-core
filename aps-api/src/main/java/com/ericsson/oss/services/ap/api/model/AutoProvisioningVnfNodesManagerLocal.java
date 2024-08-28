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
package com.ericsson.oss.services.ap.api.model;

import javax.ejb.Local;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

/**
 * Interface used to create and remove AutoProvisioningVnfNode Mos.
 * AutoProvisioningVnfNode Mos are used to track what VNF's are managed by AP
 */
@EService
@Local
public interface AutoProvisioningVnfNodesManagerLocal {

    /**
     * Creates the root AutoProvisioningVnfNodes Mo.
     * There will only be one instance of this Mo AutoProvisioningVnfNodes=1
     */
    void createAutoProvisioningVnfNodesMo();

    /**
     * Creates an AutoProvisioningVnfNode Mo for the specified vnfInsatnceId
     * AutoProvisioningVnfNode=vnfIntanceId
     * 
     * @param vnfInstanceId
     *     the instanceId of the managed VNF
     */
    void addVnfNodeInstance(String vnfInstanceId);

    /**
     * Removes an AutoProvisioningVnfNode Mo for the specified vnfInsatnceId
     * AutoProvisioningVnfNode=vnfIntanceId
     * 
     * @param vnfInstanceId
     *     the instanceId of the managed VNF
     */
    void removeVnfNodeInstance(String vnfInstanceId);
}
