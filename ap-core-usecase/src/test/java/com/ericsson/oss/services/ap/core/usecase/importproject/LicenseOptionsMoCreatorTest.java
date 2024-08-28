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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;
import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link LicenseOptionsMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LicenseOptionsMoCreatorTest {

    private static final String AP_NAMESPACE = "ap_erbs";
    private static final String AUTOMATIC_REQUEST_LICENSE_FILE = "automaticLicenseRequest";
    private static final String DPS = "dps";
    private static final String DPS_OPERATIONS = "dpsOperations";
    private static final String DUMMY_AUTO_LICENSE_REQ_FILE = "dummy_autoLicenseReq_file";
    private static final String DUMMY_FINGERPRINT = "dummy_fingerprint";
    private static final String DUMMY_LKF_FILE = "dummy_lkf_file";
    private static final String FINGERPRINT = "fingerprint";
    private static final String GROUP_ID = "groupId";
    private static final String GROUP_ID_VALUE = "949525";
    private static final String HARDWARE_TYPE = "hardwareType";
    private static final String HARDWARE_TYPE_VALUE = "BB6648";
    private static final String INSTALL_LICENSE = "installLicense";
    private static final String INSTALL_LICENSE_FALSE = "false";
    private static final String LICENSE_FILE = "licenseFile";
    private static final String LICENSE_OPTIONS = "LicenseOptions";
    private static final String LICENSE_OPTIONS_EQUALS_1 = ",LicenseOptions=1";
    private static final String NODE_TYPE = "ERBS";
    private static final String RADIO_ACCESS_TECHNOLOGIES = "radioAccessTechnologies";
    private static final List<String> RAT_VALUES_LTE_NR = Arrays.asList("LTE", "NR");
    private static final String SOFTWARE_LICENSE_TARGET_ID = "softwareLicenseTargetId";
    private static final String SWLTID = "LCS_945587_10081";
    private static final String VERSION = "1.0.0";

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private ModelData licenseModelData;

    @InjectMocks
    private LicenseOptionsMoCreator licenseOptionsMoCreator;

    @Mock
    private DpsOperations dpsOperations; // NOPMD

    private final Map<String, Object> nodeLicenseOptions = new HashMap<>();
    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    private NodeInfo nodeInfo;

    @Before
    public void setup() {
        final DataPersistenceService dpservice = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dpsOperations, DPS, dpservice);
        Whitebox.setInternalState(licenseOptionsMoCreator, DPS_OPERATIONS, dpsOperations);
        when(dpsOperations.getDataPersistenceService()).thenReturn(dpservice);
        nodeLicenseOptions.put(LICENSE_FILE, DUMMY_LKF_FILE);
        nodeLicenseOptions.put(INSTALL_LICENSE, INSTALL_LICENSE_FALSE);
        nodeLicenseOptions.put(FINGERPRINT, DUMMY_FINGERPRINT);
        nodeLicenseOptions.put(AUTOMATIC_REQUEST_LICENSE_FILE, DUMMY_AUTO_LICENSE_REQ_FILE);

        nodeInfo = new NodeInfo();
        nodeInfo.setNodeType(NODE_TYPE);
        nodeInfo.setLicenseAttributes(nodeLicenseOptions);

        when(nodeTypeMapper.getNamespace(NODE_TYPE)).thenReturn(AP_NAMESPACE);
        when(modelReader.getLatestPrimaryTypeModel(AP_NAMESPACE, LICENSE_OPTIONS)).thenReturn(licenseModelData);

        when(licenseModelData.getVersion()).thenReturn(VERSION);
        when(licenseModelData.getNameSpace()).thenReturn(AP_NAMESPACE);
    }

    @Test
    public void testLicenseMoCreated() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
            .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        licenseOptionsMoCreator.create(nodeMo, nodeInfo);
        final DpsOperations dpso = (DpsOperations) Whitebox.getInternalState(licenseOptionsMoCreator, DPS_OPERATIONS);
        final ManagedObject createdMo = dpso.getDataPersistenceService().getLiveBucket()
            .findMoByFdn(nodeDescriptor.getNodeFdn() + LICENSE_OPTIONS_EQUALS_1);
        assertEquals(DUMMY_LKF_FILE, createdMo.getAttribute(LICENSE_FILE));
        assertEquals(INSTALL_LICENSE_FALSE, createdMo.getAttribute(INSTALL_LICENSE));
        assertEquals(DUMMY_FINGERPRINT, createdMo.getAttribute(FINGERPRINT));
        assertEquals(DUMMY_AUTO_LICENSE_REQ_FILE, createdMo.getAttribute(AUTOMATIC_REQUEST_LICENSE_FILE));
    }

    @Test
    public void testLicenseMoCreatedWithAutomaticLicenseRequest() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
            .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        AutomaticLicenseRequestData automaticLicenseRequestData = AutomaticLicenseRequestDataBuilder.newBuilder()
            .with(licenseData -> {
                licenseData.groupId = GROUP_ID_VALUE;
                licenseData.hardwareType = HARDWARE_TYPE_VALUE;
                licenseData.radioAccessTechnologies = RAT_VALUES_LTE_NR;
                licenseData.softwareLicenseTargetId = SWLTID;
            })
            .build();
        nodeInfo.setAutomaticLicenseReqAttributes(automaticLicenseRequestData);
        licenseOptionsMoCreator.create(nodeMo, nodeInfo);
        final DpsOperations dpso = (DpsOperations) Whitebox.getInternalState(licenseOptionsMoCreator, DPS_OPERATIONS);
        final ManagedObject createdMo = dpso.getDataPersistenceService().getLiveBucket()
            .findMoByFdn(nodeDescriptor.getNodeFdn() + LICENSE_OPTIONS_EQUALS_1);
        assertEquals(DUMMY_LKF_FILE, createdMo.getAttribute(LICENSE_FILE));
        assertEquals(INSTALL_LICENSE_FALSE, createdMo.getAttribute(INSTALL_LICENSE));
        assertEquals(DUMMY_FINGERPRINT, createdMo.getAttribute(FINGERPRINT));
        assertEquals(DUMMY_AUTO_LICENSE_REQ_FILE, createdMo.getAttribute(AUTOMATIC_REQUEST_LICENSE_FILE));
        assertEquals(GROUP_ID_VALUE, createdMo.getAttribute(GROUP_ID));
        assertEquals(HARDWARE_TYPE_VALUE, createdMo.getAttribute(HARDWARE_TYPE));
        assertEquals(RAT_VALUES_LTE_NR, createdMo.getAttribute(RADIO_ACCESS_TECHNOLOGIES));
        assertEquals(SWLTID, createdMo.getAttribute(SOFTWARE_LICENSE_TARGET_ID));
    }
}
