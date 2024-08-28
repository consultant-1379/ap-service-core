/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Unit tests for {@link ValidateHwSerialNumberIsUniqueInApDatabase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateHwSerialNumberIsUniqueInApDatabaseTest {

    private static final String HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME = "hardwareSerialNumber";
    private static final String NODE_DIRECTORY = NODE_NAME + "/";
    private static final String VALIDATION_RULE_HW_SERIAL_NUMBER_EXISTS_IN_DATABASE_VALUE = "Project '%s' import has failed because the hardware serial number '%s' is already in use by '%s' with '%s' in AP.";
    private static final String MO_NAMESPACE = "ap";
    private static final String MO_TYPE = "Node";
    private static final String NODE_FDN1 = PROJECT_FDN + ",Node=Node2";
    private static final String NODE_FDN2 = PROJECT_FDN + ",NetworkElement=Node2";
    private static final String VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE = "Project '%s' import has failed because the hardware serial number '%s' is already in use by '%s' in ENM.";

    private final List<String> directoryNames = new ArrayList<>();

    private ValidationContext context;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutorENM;

    @Mock
    private DpsQueries dpsQueriesENM;

    @Mock
    private NodeInfo nodeInfo;

    @Mock
    private Archive archiveReader;

    @InjectMocks
    @Spy
    private ValidateHwSerialNumberIsUniqueInApDatabase validator;

    @Mock
    private ManagedObject apNodeUsingSerialNumber;

    @Mock
    private ManagedObject enmNodeUsingSerialNumber;

    @Mock
    private FDN fdn;

    @Before
    public void setUp() {
        directoryNames.add(NODE_DIRECTORY);

        final Map<String, Object> projectDataContentTarget = new HashMap<>();
        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), PROJECT_NAME);
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archiveReader);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryNames);

        context = new ValidationContext("Import", projectDataContentTarget);
        doReturn(archiveReader).when(validator).getArchive(any(ValidationContext.class));
        doReturn(nodeInfo).when(validator).getNodeInfo(any(ValidationContext.class), anyString());

        when(archiveReader.getAllDirectoryNames()).thenReturn(directoryNames);
        when(nodeInfo.getName()).thenReturn(NODE_NAME);
    }

    @Test
    public void whenHwSerialNumberIsUniqueThenValidationSucceeds() {

        final List<ManagedObject> apNodes = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();

        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodesInEnm.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn(VALID_NODE_TYPE);
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN2);

        final boolean validateResult = validator.execute(context);

        assertTrue(validateResult);
    }

    @Test
    public void whenHwSerialNumberAlreadyInUseThenValidationFails() {
        final List<ManagedObject> apNodes = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();
        nodesInEnm.add(apNodeUsingSerialNumber);
        nodesInEnm.add(enmNodeUsingSerialNumber);

        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodesInEnm.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn(VALID_NODE_TYPE);
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN2);

        validator.execute(context);

        final String expectedMessage = String.format(VALIDATION_RULE_HW_SERIAL_NUMBER_EXISTS_IN_DATABASE_VALUE, NODE_DIRECTORY, HARDWARE_SERIAL_NUMBER_VALUE, PROJECT_NAME, "Node2");
        final String expectedMessageFromENM = String.format(VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE, NODE_DIRECTORY, HARDWARE_SERIAL_NUMBER_VALUE , "Node2");
        assertEquals(String.format("%s", expectedMessage), context.getValidationErrors().get(1));
        assertEquals(String.format("%s", expectedMessageFromENM), context.getValidationErrors().get(0));
    }

    @Test
    public void whenHwSerialNumberIsNullThenValidationIsSkipped() {
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(null);
        final boolean validateResult = validator.execute(context);
        assertTrue(validateResult);
    }

    @Test
    public void whenHwSerialNumberIsEmptyThenValidationIsSkipped() {
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(" ");
        final boolean validateResult = validator.execute(context);
        assertTrue(validateResult);
        verify(dpsQueries, never()).findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE);
    }

    @Test(expected = ValidationCrudException.class)
    public void whenDpsErrorDuringQueryNodesInApNamespaceThenValidationFailsWithException() {
        when(archiveReader.getAllDirectoryNames()).thenReturn(directoryNames);
        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        doThrow(DpsPersistenceException.class).when(dpsQueryExecutor).execute();
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);

        final boolean validateResult = validator.execute(context);

        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberAlreadyInUseThenValidationFailsForEcimNodeType() {
        final String ECIM_NODETYPE = "RadioNode";
        final List<ManagedObject> apNodes = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();
        nodesInEnm.add(apNodeUsingSerialNumber);
        nodesInEnm.add(enmNodeUsingSerialNumber);

        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());

        when(dpsQueries.findMosWithAttributeValue(
            "serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "RcsHwIM", "HwItem")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodesInEnm.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn(ECIM_NODETYPE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN2);

        validator.execute(context);

        final String expectedMessageFromENM = String.format(VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE, NODE_DIRECTORY, HARDWARE_SERIAL_NUMBER_VALUE , "Node2");
        assertEquals(String.format("%s", expectedMessageFromENM), context.getValidationErrors().get(0));
    }

    @Test
    public void whenHwSerialNumberAlreadyInUseThenValidationFailsForRouterNodeType() {
        final String ER6000_NODETYPE = "Router6672";
        final List<ManagedObject> apNodes = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();
        nodesInEnm.add(apNodeUsingSerialNumber);
        nodesInEnm.add(enmNodeUsingSerialNumber);

        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);

        when(dpsQueries.findMosWithAttributeValue(
            "serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "IPR_HwIM", "HwItem")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodesInEnm.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn(ER6000_NODETYPE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN2);

        validator.execute(context);

        final String expectedMessageFromENM = String.format(VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE, NODE_DIRECTORY, HARDWARE_SERIAL_NUMBER_VALUE , "Node2");
        assertEquals(String.format("%s", expectedMessageFromENM), context.getValidationErrors().get(0));
    }

    @Test(expected = ValidationCrudException.class)
    public void whenDpsErrorDuringQueryNodesInENMNamespaceThenValidationFailsWithException() {
        final String ER6000_NODETYPE = "Router6672";
        final List<ManagedObject> apNodes = new ArrayList<>();
        apNodes.add(apNodeUsingSerialNumber);
        apNodes.add(enmNodeUsingSerialNumber);
        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);

        when(dpsQueries.findMosWithAttributeValue("serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "RcsHwIM", "HwItem")).thenReturn(dpsQueryExecutor);
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn(ER6000_NODETYPE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);

        validator.execute(context);

        final boolean validateResult = validator.execute(context);

        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberAlreadyInUseThenValidationFailsForRbsNodeType() {
        final List<ManagedObject> apNodes = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();
        nodesInEnm.add(apNodeUsingSerialNumber);
        nodesInEnm.add(enmNodeUsingSerialNumber);

        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "RBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodesInEnm.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn("RBS");
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN2);

        validator.execute(context);

        final String expectedMessageFromENM = String.format(VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE, NODE_DIRECTORY, HARDWARE_SERIAL_NUMBER_VALUE, "Node2");
        assertEquals(String.format("%s", expectedMessageFromENM), context.getValidationErrors().get(0));
    }
    @Test
    public void whenHwSerialNumberAlreadyInUseThenValidationFailsForFronthaulNodeType() {
        final List<ManagedObject> apNodes = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();
        nodesInEnm.add(apNodeUsingSerialNumber);
        nodesInEnm.add(enmNodeUsingSerialNumber);

        when(dpsQueries.findMosWithAttributeValue(HARDWARE_SERIAL_NUMBER_ATTRIBUTE_NAME, HARDWARE_SERIAL_NUMBER_VALUE, MO_NAMESPACE, MO_TYPE))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(apNodes.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(apNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN1);

        when(dpsQueries.findMosWithAttributeValue( "serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "OPTOFH_HwIM", "HwItem")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodesInEnm.iterator());
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(nodeInfo.getNodeType()).thenReturn("FRONTHAUL-6000");
        when(enmNodeUsingSerialNumber.getFdn()).thenReturn(NODE_FDN2);

        validator.execute(context);

        final String expectedMessageFromENM = String.format(VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE, NODE_DIRECTORY, HARDWARE_SERIAL_NUMBER_VALUE, "Node2");
        assertEquals(String.format("%s", expectedMessageFromENM), context.getValidationErrors().get(0));
    }
}
