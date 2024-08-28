/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseAccessException;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseFileManagerService;

/**
 * Unit tests for {@link ValidateFingerprintUnique}
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateFingerprintUniqueTest {

    private static final String FINGERPRINT_LICENSE_OPTION = "fingerprint";
    private static final String INSTALL_LICENSE = "installLicense";
    private static final String FINGERPRINT_LICENSE_OPTION_VALUE = "fingerprint_value";
    private static final String AUTOMATIC_LICENSE_REQUEST_OPTION = "automaticLicenseRequest";
    private static final String AUTOMATIC_LICENSE_REQUEST_OPTION_VALUE = "licenseRequest.xml";
    private static final String NODE_FDN = "Project=Project1,Node=Node1";
    private static final String ORDER_STARTED = "ORDER_STARTED";
    private static final String RECONFIGURATION_STARTED = "RECONFIGURATION_STARTED";
    private static final String ASSOCIATED_NODE_NAME = "Node2";
    private static final String ALREADY_ASSOCIATED_WITH_NODE = "Fingerprint fingerprint_value is already associated with Node Node2";
    private static final String ACCESS_ERROR = "accessError";

    private static final boolean INSTALL_LICENSE_VALUE = true;

    @Mock
    private LicenseFileManagerService licenseFileManagerService;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private ManagedObject licenseOptionsMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private DataBucket liveBucket;

    @InjectMocks
    @Spy
    private ValidateFingerprintUnique validationRule;

    private ValidationContext context;

    @Before
    public void setUp() {
        final Map<String, Object> contextTarget = new HashMap<>();
        contextTarget.put("nodeFdn", NODE_FDN);

        context = new ValidationContext("", contextTarget);
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn("Project=Project1,Node=Node1,LicenseOptions=1")).thenReturn(licenseOptionsMo);
        when(liveBucket.findMoByFdn("Project=Project1,Node=Node1,NodeStatus=1")).thenReturn(nodeStatusMo);

        final Map<String, Object> allLicenseOptionsMoAttributes = new HashMap<>();
        allLicenseOptionsMoAttributes.put(FINGERPRINT_LICENSE_OPTION, FINGERPRINT_LICENSE_OPTION_VALUE);
        allLicenseOptionsMoAttributes.put(INSTALL_LICENSE, INSTALL_LICENSE_VALUE);
        when(licenseOptionsMo.getAllAttributes()).thenReturn(allLicenseOptionsMoAttributes);

    }

    @Test
    public void whenValidateFingerprintUniqueAndFingerprintSpecifiedAndFingerprintNotAlreadyAssociatedWithNodeThenValidationPasses()
        throws LicenseAccessException {
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(ORDER_STARTED);
        when(licenseFileManagerService.getAssociatedNode(FINGERPRINT_LICENSE_OPTION_VALUE)).thenReturn(null);

        final boolean result = validationRule.execute(context);
        assertTrue(result);
    }

    @Test
    public void whenValidateFingerprintUniqueAndFingerprintSpecifiedAndFingerprintIsAlreadyAssociatedWithNodeThenValidationFailsAndErrorAdded()
        throws LicenseAccessException {
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(ORDER_STARTED);
        when(licenseFileManagerService.getAssociatedNode(FINGERPRINT_LICENSE_OPTION_VALUE)).thenReturn(ASSOCIATED_NODE_NAME);

        validationRule.execute(context);
        assertTrue(context.getValidationErrors().get(0).contains(ALREADY_ASSOCIATED_WITH_NODE));
    }

    @Test
    public void whenValidateFingerprintAndNodeStateNotOrderStartedThenValidationPasses() throws LicenseAccessException {
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(RECONFIGURATION_STARTED);
        when(licenseFileManagerService.getAssociatedNode(FINGERPRINT_LICENSE_OPTION_VALUE)).thenReturn(ASSOCIATED_NODE_NAME);

        final boolean result = validationRule.execute(context);
        assertTrue(result);
    }

    @Test
    public void whenSHMLicenseExceptionThrownAndInstantaneousLicensePopulatedThenErrorNotAdded() throws LicenseAccessException {
        final Map<String, Object> allLicenseOptionsMoAttributes = new HashMap<>();
        allLicenseOptionsMoAttributes.put(AUTOMATIC_LICENSE_REQUEST_OPTION, AUTOMATIC_LICENSE_REQUEST_OPTION_VALUE);
        when(licenseOptionsMo.getAllAttributes()).thenReturn(allLicenseOptionsMoAttributes);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(ORDER_STARTED);
        doThrow(new LicenseAccessException(ACCESS_ERROR)).when(licenseFileManagerService).getAssociatedNode(FINGERPRINT_LICENSE_OPTION_VALUE);

        validationRule.execute(context);
        assertTrue(context.getValidationErrors().isEmpty());
    }

    @Test
    public void whenSHMLicenseExceptionThrownAndInstantaneousLicenseNotPopulatedThenErrorAdded() throws LicenseAccessException {
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(ORDER_STARTED);
        doThrow(new LicenseAccessException(ACCESS_ERROR)).when(licenseFileManagerService).getAssociatedNode(FINGERPRINT_LICENSE_OPTION_VALUE);
        validationRule.execute(context);
        assertFalse(context.getValidationErrors().isEmpty());
    }

}
