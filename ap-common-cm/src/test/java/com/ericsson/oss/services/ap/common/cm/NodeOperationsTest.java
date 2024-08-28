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
package com.ericsson.oss.services.ap.common.cm;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.IP_ADDRESS;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.LATITUDE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.LONGITUDE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.OSS_PREFIX_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.USER_LABEL;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.model.CmNodeHeartbeatSupervisionAttribute;
import com.ericsson.oss.services.ap.common.model.FmAlarmSupervisionAttribute;
import com.ericsson.oss.services.ap.common.model.InventorySupervisionAttribute;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.PmFunctionAttribute;
import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Unit tests for {@link NodeOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeOperationsTest {

    private static final String NORMAL = "NORMAL";
    private static final String MAINTENANCE = "MAINTENANCE";
    private static final String MANUAL = "MANUAL";
    private static final String AUTOMATIC = "AUTOMATIC";
    private static final String MANAGEMENT_STATE = "managementState";
    private static final String NETWORK_ELEMENT_FDN = "NetworkElement=" + NODE_NAME;
    private static final String CMFUNCTION_FDN = NETWORK_ELEMENT_FDN + ",CmFunction=1";
    private static final String CPP_CONN_INFO_FDN = NETWORK_ELEMENT_FDN + ",CppConnectivityInformation=1";
    private static final String GEOGRAPHIC_LOCATION_FDN = NETWORK_ELEMENT_FDN + ",GeographicLocation=1";
    private static final String GEOMETRIC_POINT_FDN = GEOGRAPHIC_LOCATION_FDN + ",GeometricPoint=1";
    private static final String CM_NODE_HB_FDN = NETWORK_ELEMENT_FDN + ",CmNodeHeartbeatSupervision=1";
    private static final String FM_SUPERVISION_FDN = NETWORK_ELEMENT_FDN + ",FmAlarmSupervision=1";
    private static final String PM_SUPERVISION_FDN = NETWORK_ELEMENT_FDN + ",PmFunction=1";
    private static final String INVENTORY_SUPERVISION_FDN = NETWORK_ELEMENT_FDN + ",InventorySupervision=1";
    private static final String DELETE_NRM_DATA_ACTION = "deleteNrmDataFromEnm";
    public static final String TIMEZONE_VALUE = "Europe/Dublin";
    private static final String SUPERVISION_OPTIONS_FDN = NODE_FDN + ",SupervisionOptions=1";
    private static final ModelData NETWORK_ELEMENT_MODEL_DATA = new ModelData("OSS_NE_DEF", "1.0.0");
    private static final ModelData CONNECTIVITY_INFORMATION_MODEL_DATA = new ModelData("CPP_MED", "1.0.0");
    private static final ModelData GEOGRAPHIC_LOCATION_MODEL_DATA = new ModelData("OSS_GEO", "2.0.0");
    private static final ModelData GEOMETRIC_POINT_MODEL_DATA = new ModelData("OSS_GEO", "2.0.1");
    private static final ModelData CONNECTIVITY_INFORMATION_MODEL_DATA_EOI = new ModelData("CBPOI_MED", "1.0.0");
    private static final String CBPOI_CONN_INFO_FDN = NETWORK_ELEMENT_FDN + ",CbpOiConnectivityInformation=1";

    private final Map<String, Object> apNodeAttributes = new HashMap<>();
    private final Map<String, Object> networkElementAttributesForCbpOi = new HashMap<>();
    private final Map<String, Object> networkElementAttributes = new HashMap<>();
    private final Map<String, Object> apNodeAttributesForCbpOi = new HashMap<>();
    private final Map<String, Object> connectivityInformationAttributes = new HashMap<>();
    private final Map<String, Object> connectivityInformationAttributesForCbpOi = new HashMap<>();
    private final Map<String, Object> supervisionOptionsAttributes = new HashMap<>();
    private final Map<String, Object> supervisionOptionsCmAttributes = new HashMap<>();
    private final Map<String, Object> supervisionOptionsPmAttributes = new HashMap<>();
    private final Map<String, Object> supervisionOptionsFmAttributes = new HashMap<>();
    private final Map<String, Object> supervisionOptionsInventoryAttributes = new HashMap<>();
    private final Map<String, Object> nodeLocationAttributes = new HashMap<>();
    private final Map<String, Object> geographicLocationAttributes = new HashMap<>();
    private final Map<String, Object> geometricPointAttributes = new HashMap<>();
    private final double longitudeDouble = Double.parseDouble(LONGITUDE);
    private final double latitudeDouble = Double.parseDouble(LATITUDE);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private DpsOperations dpsOperations;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    private NodeOperations nodeOperations;

    @Before
    public void setUp() {
        nodeLocationAttributes.put("latitude", LATITUDE);
        nodeLocationAttributes.put("longitude", LONGITUDE);
        geometricPointAttributes.put("latitude", latitudeDouble);
        geometricPointAttributes.put("longitude", longitudeDouble);

        apNodeAttributes.put(NodeAttribute.NODE_LOCATION.toString(), nodeLocationAttributes);
        apNodeAttributes.put(NodeAttribute.NODE_TYPE.toString(), VALID_NODE_TYPE);
        apNodeAttributes.put(NodeAttribute.IPADDRESS.toString(), IP_ADDRESS);
        apNodeAttributes.put(NodeAttribute.NODE_IDENTIFIER.toString(), NODE_IDENTIFIER_VALUE);
        apNodeAttributes.put(NodeAttribute.OSS_PREFIX.toString(), OSS_PREFIX_VALUE);
        apNodeAttributes.put(NodeAttribute.TIMEZONE.toString(), TIMEZONE_VALUE);
        apNodeAttributes.put(NodeAttribute.USER_LABEL.toString(), USER_LABEL);

        apNodeAttributesForCbpOi.put(NodeAttribute.NODE_TYPE.toString(), "Shared-CNF");
        apNodeAttributesForCbpOi.put(NodeAttribute.IPADDRESS.toString(), IP_ADDRESS);
        apNodeAttributesForCbpOi.put(NodeAttribute.NODE_IDENTIFIER.toString(), NODE_IDENTIFIER_VALUE);
        apNodeAttributesForCbpOi.put(NodeAttribute.OSS_PREFIX.toString(), OSS_PREFIX_VALUE);
        apNodeAttributesForCbpOi.put(NodeAttribute.TIMEZONE.toString(), TIMEZONE_VALUE);
        apNodeAttributesForCbpOi.put(NodeAttribute.USER_LABEL.toString(), USER_LABEL);

        networkElementAttributes.put(NetworkElementAttribute.NE_TYPE.toString(), VALID_NODE_TYPE);
        networkElementAttributes.put(NetworkElementAttribute.PLATFORM_TYPE.toString(), "CPP");
        networkElementAttributes.put(NetworkElementAttribute.OSS_MODEL_IDENTITY.toString(), NODE_IDENTIFIER_VALUE);
        networkElementAttributes.put(NetworkElementAttribute.OSS_PREFIX.toString(), OSS_PREFIX_VALUE);
        networkElementAttributes.put(NetworkElementAttribute.TIMEZONE.toString(), TIMEZONE_VALUE);
        networkElementAttributes.put(NetworkElementAttribute.NETWORK_ELEMENT_ID.toString(), NODE_NAME);
        networkElementAttributes.put(NetworkElementAttribute.MANAGEMENT_STATE.toString(), MAINTENANCE);
        networkElementAttributes.put(NetworkElementAttribute.USER_LABEL.toString(), USER_LABEL);

        networkElementAttributesForCbpOi.put(NetworkElementAttribute.NE_TYPE.toString(), "Shared-CNF");
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.PLATFORM_TYPE.toString(), "IPOS-OI");
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.OSS_MODEL_IDENTITY.toString(), NODE_IDENTIFIER_VALUE);
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.OSS_PREFIX.toString(), OSS_PREFIX_VALUE);
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.TIMEZONE.toString(), TIMEZONE_VALUE);
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.NETWORK_ELEMENT_ID.toString(), NODE_NAME);
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.MANAGEMENT_STATE.toString(), MAINTENANCE);
        networkElementAttributesForCbpOi.put(NetworkElementAttribute.USER_LABEL.toString(), USER_LABEL);

        connectivityInformationAttributes.put("ipAddress", IP_ADDRESS);
        connectivityInformationAttributes.put("port", 80);

        connectivityInformationAttributesForCbpOi.put("ipAddress", IP_ADDRESS);
        connectivityInformationAttributesForCbpOi.put("port", 6513);

        supervisionOptionsAttributes.put(MANAGEMENT_STATE, AUTOMATIC);
        supervisionOptionsCmAttributes.put(CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), Boolean.TRUE);
        supervisionOptionsFmAttributes.put(FmAlarmSupervisionAttribute.ACTIVE.toString(), Boolean.TRUE);
        supervisionOptionsPmAttributes.put(PmFunctionAttribute.PM_ENABLED.toString(), Boolean.FALSE);
        supervisionOptionsInventoryAttributes.put(InventorySupervisionAttribute.ACTIVE.toString(), Boolean.FALSE);

        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);
    }

    @Test
    public void whenAddNodeIsSuccessfulThenNetworkElementAndConnectivityInformationAndGeographicLocationAndGeometricMosAreAlsoCreatedWithCorrectAttributes() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CPP_MED", "CppConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeographicLocation")).thenReturn(GEOGRAPHIC_LOCATION_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeometricPoint")).thenReturn(GEOMETRIC_POINT_MODEL_DATA);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CPP_MED")
            .connInfoModelName("CppConnectivityInformation")
            .addNetworkElementAttribute("platformType", "CPP")
            .addConnInformationAttribute("port", 80)
            .build();

        nodeOperations.addNode(request);

        verify(dpsOperations).createRootMo(NETWORK_ELEMENT_FDN, NETWORK_ELEMENT_MODEL_DATA, networkElementAttributes);
        verify(dpsOperations).createRootMo(CPP_CONN_INFO_FDN, CONNECTIVITY_INFORMATION_MODEL_DATA, connectivityInformationAttributes);
        verify(dpsOperations).createRootMo(GEOGRAPHIC_LOCATION_FDN, GEOGRAPHIC_LOCATION_MODEL_DATA, geographicLocationAttributes);
        verify(dpsOperations).createRootMo(GEOMETRIC_POINT_FDN, GEOMETRIC_POINT_MODEL_DATA, geometricPointAttributes);
    }

    @Test
    public void whenAddNodeIsSuccessfulThenNetworkElementAndConnectivityInformationMosAreAlsoCreatedWithCorrectAttributes() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributesForCbpOi);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CBPOI_MED", "CbpOiConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA_EOI);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CBPOI_MED")
            .connInfoModelName("CbpOiConnectivityInformation")
            .addNetworkElementAttribute("platformType", "IPOS-OI")
            .addNetworkElementAttribute("neType", "Shared-CNF")
            .addConnInformationAttribute("port", 6513)
            .build();

        nodeOperations.eoiAddNode(request);

        verify(dpsOperations).createRootMo(NETWORK_ELEMENT_FDN, NETWORK_ELEMENT_MODEL_DATA, networkElementAttributesForCbpOi);
        verify(dpsOperations).createRootMo(CBPOI_CONN_INFO_FDN, CONNECTIVITY_INFORMATION_MODEL_DATA_EOI, connectivityInformationAttributesForCbpOi);
    }

    @Test
    public void whenEoiAddNodeFailsToCreateConnectivityInfoMoThenNetworkElementMoIsDeletedAndExceptionIsPropogated() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributesForCbpOi);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CBPOI_MED", "CbpOiConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA_EOI);

        when(dpsOperations.createRootMo(CBPOI_CONN_INFO_FDN, CONNECTIVITY_INFORMATION_MODEL_DATA_EOI, connectivityInformationAttributesForCbpOi))
            .thenThrow(new IllegalStateException());

        thrown.expect(IllegalStateException.class);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CBPOI_MED")
            .addNetworkElementAttribute("platformType", "IPOS-OI")
            .connInfoModelName("CbpOiConnectivityInformation")
            .addConnInformationAttribute("port", 6513)
            .build();
        nodeOperations.eoiAddNode(request);

        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN);
    }

    @Test
    public void whenLocationNotIncludedInNodeInfoDoNotCreateGeoMos() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CPP_MED", "CppConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeographicLocation")).thenReturn(GEOGRAPHIC_LOCATION_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeometricPoint")).thenReturn(GEOMETRIC_POINT_MODEL_DATA);

        final Map<String, Object> locationEmptyMap = new HashMap<>();
        apNodeAttributes.put(NodeAttribute.NODE_LOCATION.toString(), locationEmptyMap);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CPP_MED")
            .connInfoModelName("CppConnectivityInformation")
            .addNetworkElementAttribute("platformType", "CPP")
            .addConnInformationAttribute("port", 80)
            .build();

        nodeOperations.addNode(request);
        verify(dpsOperations, times(0)).createRootMo(GEOGRAPHIC_LOCATION_FDN, GEOGRAPHIC_LOCATION_MODEL_DATA, geographicLocationAttributes);
        verify(dpsOperations, times(0)).createRootMo(GEOMETRIC_POINT_FDN, GEOMETRIC_POINT_MODEL_DATA, geometricPointAttributes);
    }

    @Test
    public void whenAddNodeFailsToCreateGeographicLocationMoThenNetworkElementMoIsDeletedAndExceptionIsPropogated() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeographicLocation")).thenReturn(GEOGRAPHIC_LOCATION_MODEL_DATA);

        when(dpsOperations.createRootMo(GEOGRAPHIC_LOCATION_FDN, GEOGRAPHIC_LOCATION_MODEL_DATA, geographicLocationAttributes))
            .thenThrow(new IllegalStateException());

        thrown.expect(IllegalStateException.class);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .addNetworkElementAttribute("platformType", "CPP")
            .build();
        nodeOperations.addNode(request);

        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN);
    }

    @Test
    public void whenAddNodeFailsToCreateGeometricPointMoThenNetworkElementMoIsDeletedAndExceptionIsPropogated() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeographicLocation")).thenReturn(GEOGRAPHIC_LOCATION_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeometricPoint")).thenReturn(GEOMETRIC_POINT_MODEL_DATA);

        when(dpsOperations.createRootMo(GEOMETRIC_POINT_FDN, GEOMETRIC_POINT_MODEL_DATA, geometricPointAttributes))
            .thenThrow(new IllegalStateException());

        thrown.expect(IllegalStateException.class);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .addNetworkElementAttribute("platformType", "CPP")
            .build();
        nodeOperations.addNode(request);

        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN);
    }

    @Test
    public void whenLocationInNodeInfoIsEmptyDoNotCreateGeographicLocationAndGeometricPointMos() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeographicLocation")).thenReturn(GEOGRAPHIC_LOCATION_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("OSS_GEO", "GeometricPoint")).thenReturn(GEOMETRIC_POINT_MODEL_DATA);

        nodeLocationAttributes.clear();
        apNodeAttributes.put(NodeAttribute.NODE_LOCATION.toString(), nodeLocationAttributes);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CPP_MED")
            .connInfoModelName("CppConnectivityInformation")
            .addNetworkElementAttribute("platformType", "CPP")
            .addConnInformationAttribute("port", 80)
            .build();

        nodeOperations.addNode(request);

        verify(dpsOperations, times(0)).createRootMo(GEOGRAPHIC_LOCATION_FDN, GEOGRAPHIC_LOCATION_MODEL_DATA, geographicLocationAttributes);
        verify(dpsOperations, times(0)).createRootMo(GEOMETRIC_POINT_FDN, GEOMETRIC_POINT_MODEL_DATA, geometricPointAttributes);
    }

    @Test
    public void whenUpdateNodeAttributeThenAttributeIsUpdated() {
        nodeOperations.updateNode(NODE_NAME, networkElementAttributes);
        verify(dpsOperations).updateMo(NETWORK_ELEMENT_FDN, networkElementAttributes);
    }

    @Test
    public void whenUpdateNodeAttributesThenAttributesAreUpdated() {
        nodeOperations.updateNode(NODE_NAME, "Dummy_Attribute_Key", "Dummy_Attribute_Value");
        verify(dpsOperations).updateMo(NETWORK_ELEMENT_FDN, "Dummy_Attribute_Key", "Dummy_Attribute_Value");
    }

    @Test
    public void whenAddNodeIsSuccessfulAndOssPrefixIsEmptyThenNetworkElementAndConnectivityInfoMosAreCreatedWithCorrectAttributes() {
        apNodeAttributes.remove(NodeAttribute.OSS_PREFIX.toString());
        networkElementAttributes.remove(NetworkElementAttribute.OSS_PREFIX.toString());
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CPP_MED", "CppConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CPP_MED")
            .connInfoModelName("CppConnectivityInformation")
            .addNetworkElementAttribute("platformType", "CPP")
            .addConnInformationAttribute("port", 80)
            .build();

        nodeOperations.addNode(request);

        verify(dpsOperations).createRootMo(NETWORK_ELEMENT_FDN, NETWORK_ELEMENT_MODEL_DATA, networkElementAttributes);
        verify(dpsOperations).createRootMo(CPP_CONN_INFO_FDN, CONNECTIVITY_INFORMATION_MODEL_DATA, connectivityInformationAttributes);
    }

    @Test(expected = ApServiceException.class)
    public void whenAddNodeFailsToCreateNetworkElementThenCreateIsRetriedFiveTimesThenApServiceExceptionIsPropagated() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CPP_MED", "CppConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA);
        doThrow(Exception.class).when(dpsOperations).createRootMo(NETWORK_ELEMENT_FDN, NETWORK_ELEMENT_MODEL_DATA, networkElementAttributes);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CPP_MED")
            .connInfoModelName("CppConnectivityInformation")
            .addNetworkElementAttribute("platformType", "CPP")
            .addConnInformationAttribute("port", 80)
            .build();

        nodeOperations.addNode(request);

        verify(dpsOperations, times(5)).createRootMo(NETWORK_ELEMENT_FDN, NETWORK_ELEMENT_MODEL_DATA, networkElementAttributes);
        verify(dpsOperations).createRootMo(CPP_CONN_INFO_FDN, CONNECTIVITY_INFORMATION_MODEL_DATA, connectivityInformationAttributes);
    }

    @Test
    public void whenAddNodeFailsToCreateConnectivityInfoMoThenNetworkElementMoIsDeletedAndExceptionIsPropogated() {
        when(dpsOperations.readMoAttributes(NODE_FDN)).thenReturn(apNodeAttributes);
        when(modelReader.getLatestPrimaryTypeModel("OSS_NE_DEF", "NetworkElement")).thenReturn(NETWORK_ELEMENT_MODEL_DATA);
        when(modelReader.getLatestPrimaryTypeModel("CPP_MED", "CppConnectivityInformation")).thenReturn(CONNECTIVITY_INFORMATION_MODEL_DATA);

        when(dpsOperations.createRootMo(CPP_CONN_INFO_FDN, CONNECTIVITY_INFORMATION_MODEL_DATA, connectivityInformationAttributes))
            .thenThrow(new IllegalStateException());

        thrown.expect(IllegalStateException.class);

        final AddNodeRequest request = new AddNodeRequest.Builder(NODE_FDN)
            .connInfoNamespace("CPP_MED")
            .addNetworkElementAttribute("platformType", "CPP")
            .connInfoModelName("CppConnectivityInformation")
            .addConnInformationAttribute("port", 80)
            .build();
        nodeOperations.addNode(request);

        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN);
    }

    @Test
    public void whenRemoveNodeIsSuccessfulThenNetworkElementAndChildrenAreDeleted() {
        nodeOperations.removeNode(NODE_NAME);

        verify(dpsOperations).updateMo(CM_NODE_HB_FDN, CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).updateMo(FM_SUPERVISION_FDN, FmAlarmSupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).updateMo(PM_SUPERVISION_FDN, PmFunctionAttribute.PM_ENABLED.toString(), false);
        verify(dpsOperations).updateMo(INVENTORY_SUPERVISION_FDN, InventorySupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).performMoAction(CMFUNCTION_FDN, DELETE_NRM_DATA_ACTION);
        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN);
    }
    @Test
    public void whenEoiRemoveNodeIsSuccessfulThenNetworkElementAndChildrenAreDeleted() {
        nodeOperations.eoiRemoveNode(NODE_NAME);

        verify(dpsOperations).updateMo(CM_NODE_HB_FDN, CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).updateMo(FM_SUPERVISION_FDN, FmAlarmSupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).updateMo(PM_SUPERVISION_FDN, PmFunctionAttribute.PM_ENABLED.toString(), false);
        verify(dpsOperations).performMoAction(CMFUNCTION_FDN, DELETE_NRM_DATA_ACTION);
        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN);
    }

    @Test
    public void whenRemoveNodeExecutedAndDeleteNrmFailedThenExceptionIsSuppressed() {
        doThrow(new ApServiceException(new Exception("error"))).when(dpsOperations).performMoAction(CMFUNCTION_FDN, DELETE_NRM_DATA_ACTION);
        nodeOperations.removeNode(NODE_NAME);
        verify(dpsOperations).updateMo(CM_NODE_HB_FDN, CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).updateMo(FM_SUPERVISION_FDN, FmAlarmSupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).updateMo(PM_SUPERVISION_FDN, PmFunctionAttribute.PM_ENABLED.toString(), false);
        verify(dpsOperations).updateMo(INVENTORY_SUPERVISION_FDN, InventorySupervisionAttribute.ACTIVE.toString(), false);
        verify(dpsOperations).deleteMo(NETWORK_ELEMENT_FDN); // We still attempt to delete the NE, though mediation will cause this to fail on ENM
    }

    @Test
    public void whenEnableCmSupervisionThenCmSupervisionIsUpdated() {
        nodeOperations.activateCmNodeHeartbeatSupervision(NODE_NAME);
        verify(dpsOperations).updateMo(CM_NODE_HB_FDN, CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), true);
    }

    @Test
    public void whenDisableCmSupervisionThenCmSupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.CM, false);
        verify(dpsOperations).updateMo(CM_NODE_HB_FDN, CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), false);
    }

    @Test
    public void whenGetCmSupervisionThenReturnCmSupervision() {
        when(dpsOperations.readMoAttributes(CM_NODE_HB_FDN)).thenReturn(supervisionOptionsCmAttributes);
        final boolean cmSupervision = nodeOperations.getSupervisionStatus(NODE_NAME, SupervisionMoType.CM);
        verify(dpsOperations).readMoAttributes(CM_NODE_HB_FDN);
        assertTrue(cmSupervision);
    }

    @Test
    public void whenEnableFmSupervisionThenFmSupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.FM, true);
        verify(dpsOperations).updateMo(FM_SUPERVISION_FDN, FmAlarmSupervisionAttribute.ACTIVE.toString(), true);
    }

    @Test
    public void whenDisableFmSupervisionThenFmSupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.FM, false);
        verify(dpsOperations).updateMo(FM_SUPERVISION_FDN, FmAlarmSupervisionAttribute.ACTIVE.toString(), false);
    }

    @Test
    public void whenGetFmSupervisionThenReturnFmSupervision() {
        when(dpsOperations.readMoAttributes(FM_SUPERVISION_FDN)).thenReturn(supervisionOptionsFmAttributes);
        final boolean fmSupervision = nodeOperations.getSupervisionStatus(NODE_NAME, SupervisionMoType.FM);
        verify(dpsOperations).readMoAttributes(FM_SUPERVISION_FDN);
        assertTrue(fmSupervision);
    }

    @Test
    public void whenEnablePmSupervisionThenPmSupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.PM, true);
        verify(dpsOperations).updateMo(PM_SUPERVISION_FDN, PmFunctionAttribute.PM_ENABLED.toString(), true);
    }

    @Test
    public void whenDisablePmSupervisionThenPmSupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.PM, false);
        verify(dpsOperations).updateMo(PM_SUPERVISION_FDN, PmFunctionAttribute.PM_ENABLED.toString(), false);
    }

    @Test
    public void whenGetPmSupervisionThenReturnPmSupervision() {
        when(dpsOperations.readMoAttributes(PM_SUPERVISION_FDN)).thenReturn(supervisionOptionsPmAttributes);
        final boolean pmSupervision = nodeOperations.getSupervisionStatus(NODE_NAME, SupervisionMoType.PM);
        verify(dpsOperations).readMoAttributes(PM_SUPERVISION_FDN);
        assertFalse(pmSupervision);
    }

    @Test
    public void whenEnableInventorySupervisionThenInventorySupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.INVENTORY, true);
        verify(dpsOperations).updateMo(INVENTORY_SUPERVISION_FDN, InventorySupervisionAttribute.ACTIVE.toString(), true);
    }

    @Test
    public void whenDisableInventorySupervisionThenInventorySupervisionIsUpdated() {
        nodeOperations.setSupervisionStatus(NODE_NAME, SupervisionMoType.INVENTORY, false);
        verify(dpsOperations).updateMo(INVENTORY_SUPERVISION_FDN, InventorySupervisionAttribute.ACTIVE.toString(), false);
    }

    @Test
    public void whenGetInventorySupervisionThenReturnInventorySupervision() {
        when(dpsOperations.readMoAttributes(INVENTORY_SUPERVISION_FDN)).thenReturn(supervisionOptionsInventoryAttributes);
        final boolean InvSupervision = nodeOperations.getSupervisionStatus(NODE_NAME, SupervisionMoType.INVENTORY);
        verify(dpsOperations).readMoAttributes(INVENTORY_SUPERVISION_FDN);
        assertFalse(InvSupervision);
    }

    @Test
    public void whenMaintenanceAutoThenManagementStateIsUpdated() {
        when(dpsOperations.readMoAttributes(SUPERVISION_OPTIONS_FDN)).thenReturn(supervisionOptionsAttributes);
        final boolean validUpdate = nodeOperations.isUpdateValid(NORMAL, NODE_FDN);
        assertTrue(validUpdate);
        nodeOperations.setManagementState(NODE_FDN, NORMAL);
        verify(dpsOperations).updateMo(NETWORK_ELEMENT_FDN, NetworkElementAttribute.MANAGEMENT_STATE.toString(), NORMAL);
    }

    @Test
    public void whenMaintenanceManualThenManagementStateIsNotUpdated() {
        supervisionOptionsAttributes.put(MANAGEMENT_STATE, MANUAL);
        when(dpsOperations.existsMoByFdn(SUPERVISION_OPTIONS_FDN)).thenReturn(true);
        when(dpsOperations.readMoAttributes(SUPERVISION_OPTIONS_FDN)).thenReturn(supervisionOptionsAttributes);
        final boolean validUpdate = nodeOperations.isUpdateValid(NORMAL, NODE_FDN);
        assertFalse(validUpdate);
    }
}
