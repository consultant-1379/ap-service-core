/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.eoi;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
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

import java.util.*;

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EoiValidateNodeNameUniqueinNRMTest {


    private static final String NODE_NAME_2 = "Node2";
    private static final String NODE_NAME_3 = "Node3";
    private static final String NODE2_FDN = PROJECT_FDN + ",Node=" + NODE_NAME_2;
    private static final String NODE3_FDN = PROJECT_FDN + ",Node=" + NODE_NAME_3;

    private static final String NODE_ALREADY_EXISTS_MESSAGE_FORMAT = "ENM node already exists NetworkElement=%s";

    private final List<ManagedObject> emptyNodeMos = Collections.<ManagedObject>emptyList();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueries.DpsQueryExecutor<ManagedObject> dpsQueryExecutor;


    private static final Map<String, Object> networkElement = new HashMap<>();
    private static final Map<String, Object> networkElement1 = new HashMap<>();
    private static final Map<String, Object> networkElement2 = new HashMap<>();
    private static final Map<String, Object> networkElements = new HashMap<>();
    final Map<String, Map<String, Object>> validationTarget = new HashMap<>();


    private ValidationContext context;

    @InjectMocks
    @Spy
    private EoiValidateNodeNameUniqueInNrm eoiValidateNodeNameUniqueInNrm;

    @Before
    public void setUp() {
        networkElement.put("nodeName", NODE_NAME);
        networkElement1.put("nodeName", NODE_NAME_2);
        networkElement2.put("nodeName", NODE_NAME_3);
        final List finalList = new ArrayList();
        finalList.add(networkElement);
        finalList.add(networkElement1);
        finalList.add(networkElement2);
        networkElements.put("networkelements", finalList);
        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);
        context = new ValidationContext("Import", validationTarget);
    }

    @Test
    public void whenNoNetworkElementExistsWithSameNameThenReturnTrue() {
        when(dpsQueries.findMoByName(anyString(), eq(NETWORK_ELEMENT.toString()), eq(OSS_NE_DEF.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos.iterator());
        final boolean result = eoiValidateNodeNameUniqueInNrm.execute(context);
        assertTrue("The validation failed for node names without duplication in the db", result);
    }

    @Test
    public void whenErrorReadingNodesInApNamespaceThenRuleThrowsException() {
        when(dpsQueries.findMoByName(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        doThrow(DpsPersistenceException.class).when(dpsQueryExecutor).execute();

        exception.expect(ValidationCrudException.class);

        eoiValidateNodeNameUniqueInNrm.execute(context);

        assertEquals("Error", context.getValidationErrors().get(0));
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

        eoiValidateNodeNameUniqueInNrm.execute(context);

        assertEquals(String.format(NODE_ALREADY_EXISTS_MESSAGE_FORMAT, NODE_NAME_2), context.getValidationErrors().get(0));
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

        eoiValidateNodeNameUniqueInNrm.execute(context);

        assertEquals(String.format(NODE_ALREADY_EXISTS_MESSAGE_FORMAT, NODE_NAME_2), context.getValidationErrors().get(0));
        assertEquals(String.format(NODE_ALREADY_EXISTS_MESSAGE_FORMAT, NODE_NAME_3), context.getValidationErrors().get(1));
    }


}
