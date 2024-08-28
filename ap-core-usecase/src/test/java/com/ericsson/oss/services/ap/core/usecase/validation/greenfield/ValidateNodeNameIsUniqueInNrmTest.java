/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

/**
 * Unit tests for {@link ValidateNodeNameIsUniqueInNrm).
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeNameIsUniqueInNrmTest {

    private static final String NODE_NAME_2 = "Node2";
    private static final String NODE_NAME_3 = "Node3";
    private static final String DIRECTORY_1 = "DirectoryOne";
    private static final String DIRECTORY_2 = "DirectoryTwo";
    private static final String DIRECTORY_3 = "DirectoryThree";
    private static final String NODE2_FDN = PROJECT_FDN + ",Node=" + NODE_NAME_2;
    private static final String NODE3_FDN = PROJECT_FDN + ",Node=" + NODE_NAME_3;

    private static final String NODE_ALREADY_EXISTS_MESSAGE_FORMAT = "%s - ENM node already exists NetworkElement=%s";

    private final List<ManagedObject> emptyNodeMos = Collections.<ManagedObject> emptyList();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private Archive archiveReader;

    @Mock
    private NodeInfo nodeInfo;

    @InjectMocks
    @Spy
    private ValidateNodeNameIsUniqueInNrm validator;

    private ValidationContext context;

    @Before
    public void setUp() {
        final List<String> directoryNames = Arrays.asList(DIRECTORY_1, DIRECTORY_2, DIRECTORY_3);
        when(archiveReader.getAllDirectoryNames()).thenReturn(directoryNames);

        when(nodeInfo.getName()).thenReturn(NODE_NAME).thenReturn(NODE_NAME_2).thenReturn(NODE_NAME_3);

        final Map<String, Object> projectDataContentTarget = new HashMap<>();
        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), PROJECT_NAME);
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archiveReader);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryNames);
        context = new ValidationContext("Import", projectDataContentTarget);

        doReturn(archiveReader).when(validator).getArchive(any(ValidationContext.class));
        doReturn(nodeInfo).when(validator).getNodeInfo(any(ValidationContext.class), anyString());
    }

    @Test
    public void whenNoNetworkElementExistsWithSameNameThenReturnTrue() {
        when(dpsQueries.findMoByName(anyString(), eq(NETWORK_ELEMENT.toString()), eq(OSS_NE_DEF.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos.iterator());
        final boolean result = validator.execute(context);
        assertTrue("The validation failed for node names without duplication in the db", result);
    }

    @Test
    public void whenNetworkElementWithSameNameExistsForSingleNodeInProjectThenReturnFalse() {
        final List<ManagedObject> existingNodeMos = new ArrayList<>();
        final ManagedObject existingNodeMo = Mockito.mock(ManagedObject.class);
        existingNodeMos.add(existingNodeMo);

        when(dpsQueries.findMoByName(anyString(), eq(NETWORK_ELEMENT.toString()), eq(OSS_NE_DEF.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos.iterator()).thenReturn(existingNodeMos.iterator())
                .thenReturn(emptyNodeMos.iterator());
        when(existingNodeMo.getFdn()).thenReturn(NODE2_FDN);

        validator.execute(context);

        assertEquals(String.format(NODE_ALREADY_EXISTS_MESSAGE_FORMAT, DIRECTORY_2, NODE_NAME_2), context.getValidationErrors().get(0));
    }

    @Test
    public void whenNetworkElementWithSameNameExistsForMultipleNodesInProjectThenReturnFalse() {
        final List<ManagedObject> existingNodeMos = new ArrayList<>();
        final ManagedObject existingNodeMo = Mockito.mock(ManagedObject.class);
        existingNodeMos.add(existingNodeMo);

        when(dpsQueries.findMoByName(anyString(), eq(NETWORK_ELEMENT.toString()), eq(OSS_NE_DEF.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos.iterator()).thenReturn(existingNodeMos.iterator())
                .thenReturn(existingNodeMos.iterator());
        when(existingNodeMo.getFdn()).thenReturn(NODE2_FDN).thenReturn(NODE3_FDN);

        validator.execute(context);

        assertEquals(String.format(NODE_ALREADY_EXISTS_MESSAGE_FORMAT, DIRECTORY_3, NODE_NAME_3), context.getValidationErrors().get(0));
        assertEquals(String.format(NODE_ALREADY_EXISTS_MESSAGE_FORMAT, DIRECTORY_2, NODE_NAME_2), context.getValidationErrors().get(1));
    }

    @Test
    public void whenErrorReadingNodesInApNamespaceThenRuleThrowsException() {
        when(dpsQueries.findMoByName(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        doThrow(DpsPersistenceException.class).when(dpsQueryExecutor).execute();

        exception.expect(ValidationCrudException.class);

        validator.execute(context);

        assertEquals("Error", context.getValidationErrors().get(0));
    }
}
