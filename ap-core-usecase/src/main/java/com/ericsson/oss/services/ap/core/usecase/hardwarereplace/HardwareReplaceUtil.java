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
package com.ericsson.oss.services.ap.core.usecase.hardwarereplace;

import static com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes.SECURE_PASSWORD;
import static com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes.SECURE_USERNAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.util.AbstractApActivityUtil;
import com.ericsson.oss.services.ap.model.NodeType;

/**
 * Utility class used to create the MOs required to perform a Hardware Replace.
 */
public class HardwareReplaceUtil extends AbstractApActivityUtil {

    private static final String NETWORK_ELEMENT_SECURITY_SECURE_USER_ATTRIBUTE = "secureUserName";
    private static final String NETWORK_ELEMENT_SECURITY_SECURE_PASS_WORD_ATTRIBUTE = "secureUserPassword";

    @Inject
    private HardwareReplaceModelCreator hardwareReplaceModelCreator;

    @Inject
    private DhcpInfoCreator dhcpInfoCreator;

    @Inject
    private MRExecutionRecorder mrExecRecorder;

    /**
     * Creates the AP Node MO and child MOs.
     *
     * @param projectFdn
     *            FDN of the project for Hardware Replace
     * @param nodeInfo
     *            link to NodeInfo Object for elements supplied in nodeInfo.xml
     */
    public void create(final String projectFdn, final NodeInfo nodeInfo) {
        createApNodeMo(projectFdn, nodeInfo);
    }

    private ManagedObject createApNodeMo(final String projectFdn, final NodeInfo nodeInfo) {
        final ManagedObject networkElementMo = getNetworkElementMo(nodeInfo);
        final String nodeType = networkElementMo.getAttribute(NetworkElementAttribute.NE_TYPE.toString());
        recordMrExecution(nodeInfo, nodeType);
        final Map<String, Object> nodeAttributes = new HashMap<>();
        final ManagedObject securityFunctionMo = getSecurityFunctionMo(networkElementMo);
        final ManagedObject networkElementSecurityMo = getNetworkElementSecurityMo(securityFunctionMo);
        final Map<String, Object> nodeUserCredentials = getNodeUserCredentials(networkElementSecurityMo);
        final List<String> workflowInstanceIdList = new ArrayList<>();
        nodeInfo.setNodeUserCredentialAttributes(nodeUserCredentials);

        nodeAttributes.put(NodeAttribute.WORKFLOW_INSTANCE_ID_LIST.toString(), workflowInstanceIdList);
        nodeAttributes.put(NodeAttribute.NODE_TYPE.toString(), nodeType);
        nodeAttributes.put(NodeAttribute.IPADDRESS.toString(), getNodeIpAddress(networkElementMo));
        nodeAttributes.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), Boolean.TRUE);
        nodeAttributes.put(NodeAttribute.NODE_IDENTIFIER.toString(),
            networkElementMo.getAttribute(NetworkElementAttribute.OSS_MODEL_IDENTITY.toString()));
        nodeAttributes.put(NodeAttribute.OSS_PREFIX.toString(), networkElementMo.getAttribute(NetworkElementAttribute.OSS_PREFIX.toString()));
        nodeAttributes.put(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), nodeInfo.getHardwareSerialNumber());
        nodeAttributes.put(NodeAttribute.BACKUP_NAME.toString(), nodeInfo.getBackupName());
        nodeInfo.setNodeAttributes(nodeAttributes);

        dhcpInfoCreator.create(nodeInfo, nodeType, networkElementMo);

        return hardwareReplaceModelCreator.create(nodeInfo, projectFdn);
    }

    private void recordMrExecution(final NodeInfo nodeData, final String nodeType) {
        if (nodeData.getBackupName() != null) {
            mrExecRecorder.recordMRExecution(MRDefinition.AP_HARDWAREREPLACE_BACKUP);
        }
        if (nodeData.getHardwareSerialNumber() == null) {
            mrExecRecorder.recordMRExecution(MRDefinition.AP_HARDWAREREPLACE_NO_SERIALNUMBER);
        }
        if (nodeType.equals(NodeType.Controller6610.toString())) {
            mrExecRecorder.recordMRExecution(MRDefinition.AP_HARDWAREREPLACE_CONTROLLER6610);
        }
    }

    private static ManagedObject getSecurityFunctionMo(final ManagedObject networkElementMo) {
        return networkElementMo.getChild(MoType.SECURITY_FUNCTION + "=1");
    }

    private static ManagedObject getNetworkElementSecurityMo(final ManagedObject securityFunctionMo) {
        return securityFunctionMo.getChild(MoType.NETWORK_ELEMENT_SECURITY + "=1");
    }

    private static Map<String, Object> getNodeUserCredentials(final ManagedObject networkElementSecurityMo) {
        final Map<String, Object> nodeSecurityAttributes = new HashMap<>();
        final String secureUser = networkElementSecurityMo.getAttribute(NETWORK_ELEMENT_SECURITY_SECURE_USER_ATTRIBUTE);
        final String securePassword = networkElementSecurityMo.getAttribute(NETWORK_ELEMENT_SECURITY_SECURE_PASS_WORD_ATTRIBUTE);
        nodeSecurityAttributes.put(SECURE_USERNAME.toString(), secureUser);
        nodeSecurityAttributes.put(SECURE_PASSWORD.toString(), securePassword);
        return nodeSecurityAttributes;
    }

}
