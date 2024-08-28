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

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EoiValidateNodeTypeSupportedTest {

    private static final String VALID_NODE_TYPE_SHARED = "SHARED_CNF";
    private ValidationContext context;

    @Mock
    private ModelReader modelReader;

    @Mock
    private EnumDataTypeSpecification enumSpecification;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    private static final String NODE_NAME1 = "NodeName1";
    private static final String NODE_NAME2 = "NodeName2";
    private static final String NODE_TYPE = "SHARED_CNF";
    private static final String INVALID_NODE_TYPE = "INVALID";


    @InjectMocks
    @Spy
    private EoiValidateNodeTypeSupported eoiValidateNodeTypeSupported;

    private static final List<String> SUPPORTED_NODE_TYPES = Arrays.asList("ERBS", "MSRBS_V1", "Router6000-2", "Router60002", "SHARED_CNF");

    private static final Map<String, Object> networkElement = new HashMap<>();
    private static final Map<String, Object> networkElement1 = new HashMap<>();
    private static final Map<String, Object> networkElements = new HashMap<>();
    final Map<String, Map<String, Object>> validationTarget = new HashMap<>();

    @Before
    public void setUp() {

        when(modelReader.getLatestEnumDataTypeSpecification(SchemaConstants.OSS_EDT, "ap", "NodeType")).thenReturn(enumSpecification);
        when(modelReader.getSupportedNodeTypes()).thenReturn(SUPPORTED_NODE_TYPES);
        when(enumSpecification.getMemberNames()).thenReturn(SUPPORTED_NODE_TYPES);


    }

    @Test
    public void testRuleReturnsTrueWhenNodeTypeAreSupportedForAllNodesInProject() {
        when(nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE_SHARED)).thenReturn(VALID_NODE_TYPE_SHARED);
        networkElement.put("name", NODE_NAME2);
        networkElement.put("nodeType", NODE_TYPE);
        final List list = Arrays.asList(networkElement);
        networkElements.put("networkelements", list);
        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);
        context = new ValidationContext("Import", validationTarget);
        final boolean result = eoiValidateNodeTypeSupported.execute(context);
        assertTrue(result);
    }

    @Test
    public void testRuleFailsWhenNodeTypeAreMissedForNodesInProject() {
        when(nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE_SHARED)).thenReturn(VALID_NODE_TYPE_SHARED);
        networkElement.put("name", NODE_NAME1);
        networkElement.put("nodeType", "");
        networkElement1.put("name", NODE_NAME2);
        networkElement1.put("nodeType", "");
        final List finalList = new ArrayList();
        finalList.add(networkElement);
        finalList.add(networkElement1);

        networkElements.put("networkelements", finalList);

        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);

        context = new ValidationContext("Import", validationTarget);
        eoiValidateNodeTypeSupported.execute(context);
        assertEquals(2, context.getValidationErrors().size());
        final String errorMessage = String.format("The value of node attribute %s is not set in network element.", "nodeType");
        assertEquals(String.format("%s", errorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("%s", errorMessage), context.getValidationErrors().get(1));
    }


    @Test
    public void testRuleFailsWhenNodeTypeAreNotSupportedForNodesInProject() {
        when(nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE_SHARED)).thenReturn(VALID_NODE_TYPE_SHARED);
        networkElement.put("name", NODE_NAME1);
        networkElement.put("nodeType", INVALID_NODE_TYPE);
        networkElement1.put("name", NODE_NAME2);
        networkElement1.put("nodeType", INVALID_NODE_TYPE);
        final List finalList = new ArrayList();
        finalList.add(networkElement);
        finalList.add(networkElement1);
        networkElements.put("networkelements", finalList);

        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);

        context = new ValidationContext("Import", validationTarget);

        eoiValidateNodeTypeSupported.execute(context);

        assertEquals(2, context.getValidationErrors().size());
        final String errorMessage = String.format("Unsupported node type %s in network element. Valid node types are: %s", INVALID_NODE_TYPE,
            SUPPORTED_NODE_TYPES);
        assertEquals(String.format("%s", errorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("%s", errorMessage), context.getValidationErrors().get(1));
    }


}
