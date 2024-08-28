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

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
 * Unit tests for {@link ValidateNodeNameIsUniqueInAp}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeNameIsUniqueInApTest {

    private static final String NODE_NAME_2 = "Node2";
    private static final String NODE_NAME_3 = "Node3";
    private static final String DIRECTORY_1 = "DirectoryOne";
    private static final String DIRECTORY_2 = "DirectoryTwo";
    private static final String DIRECTORY_3 = "DirectoryThree";
    private static final String NODE2_FDN = PROJECT_FDN + ",Node=" + NODE_NAME_2;
    private static final String NODE3_FDN = PROJECT_FDN + ",Node=" + NODE_NAME_3;

    private ValidationContext context = null;
    private final Iterator<ManagedObject> emptyNodeMos = Collections.<ManagedObject> emptyIterator();

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
    private ValidateNodeNameIsUniqueInAp validator;

    @Before
    public void setUp() {
        final List<String> directoryNames = Arrays.asList(DIRECTORY_1, DIRECTORY_2, DIRECTORY_3);

        final Map<String, Object> validationTarget = new HashMap<>();
        validationTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryNames);

        when(archiveReader.getAllDirectoryNames()).thenReturn(directoryNames);

        context = new ValidationContext("Import", validationTarget);
        when(nodeInfo.getName()).thenReturn(NODE_NAME).thenReturn(NODE_NAME_2).thenReturn(NODE_NAME_3);
        doReturn(archiveReader).when(validator).getArchive(any(ValidationContext.class));
        doReturn(nodeInfo).when(validator).getNodeInfo(any(ValidationContext.class), anyString());
    }

    @Test
    public void whenAllNodeNamesInProjectAreUniqueInApNamespaceThenReturnTrue() {
        when(dpsQueries.findMoByName(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos);
        final boolean result = validator.execute(context);
        assertTrue("The validation failed for node names without duplication in the db", result);
    }

    @Test
    public void whenSingleNodeNameInProjectIsNotUniqueInApNamespaceThenReturnFalse() {
        final List<ManagedObject> existingNodeMos = new ArrayList<>();
        final ManagedObject existingNodeMo = Mockito.mock(ManagedObject.class);
        existingNodeMos.add(existingNodeMo);

        when(dpsQueries.findMoByName(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos).thenReturn(existingNodeMos.iterator()).thenReturn(emptyNodeMos);
        when(existingNodeMo.getFdn()).thenReturn(NODE2_FDN);

        validator.execute(context);

        assertEquals(String.format("%s - Node=%s already exists in project %s", DIRECTORY_2, NODE_NAME_2, PROJECT_NAME),
                context.getValidationErrors().get(0));
    }

    @Test
    public void whenMultipleNodeNamesInProjectAreNotUniqueInApNamespaceThenReturnFalse() {
        final List<ManagedObject> existingNodeMos = new ArrayList<>();
        final ManagedObject existingNodeMo = Mockito.mock(ManagedObject.class);
        existingNodeMos.add(existingNodeMo);

        when(dpsQueries.findMoByName(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos).thenReturn(existingNodeMos.iterator()).thenReturn(existingNodeMos.iterator());
        when(existingNodeMo.getFdn()).thenReturn(NODE2_FDN).thenReturn(NODE3_FDN);

        validator.execute(context);

        assertEquals(String.format("%s - Node=%s already exists in project %s", DIRECTORY_3, NODE_NAME_3, PROJECT_NAME),
                context.getValidationErrors().get(0));
        assertEquals(String.format("%s - Node=%s already exists in project %s", DIRECTORY_2, NODE_NAME_2, PROJECT_NAME),
                context.getValidationErrors().get(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenErrorReadingNodesInApNamespaceThenRuleThrowsException() {
        when(dpsQueries.findMoByName(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenThrow(DpsPersistenceException.class);

        exception.expect(ValidationCrudException.class);
        validator.execute(context);

        assertEquals("Context returned the wrong validation error message", "Error", context.getValidationErrors().get(0));
    }
}
