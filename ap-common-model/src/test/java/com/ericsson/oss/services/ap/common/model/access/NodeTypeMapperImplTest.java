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
package com.ericsson.oss.services.ap.common.model.access;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.exception.UnknownModelException;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.TypedModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.typed.capabilities.CapabilityInformation;

/**
 * Unit tests for {@link NodeTypeMapperImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeTypeMapperImplTest {


    private static final String DEFAULT_INTERNAL_NODE_TYPE = "ecim";
    private static final String NODE_TYPE_NAMESPACE_FORMAT = "ap_%s";
    private static final String VALID_NODE_TYPE = "ERBS";
    private static final String VALID_NODE_TYPE_FH = "FRONTHAUL6000";
    private static final String VALID_NODE_TYPE_R6K= "Router60002";
    private static final String VNF_INTERNAL_NODE_TYPE = "vnf";
    private static final String WRONG_NODE_TYPE = "WRONG_NODE_TYPE";
    private static final String NAME_SPACE_ECIM = "ap_ecim";
    private static final String NAME_SPACE_ERBS = "ap_erbs";
    private static final String NAME_SPACE_SHAREDCNF = "ap_SharedCNF";
    private static final String NAME_SPACE_ROUTER = "ap_Router60002";
    private static final String NAME_SPACE_FRONTHAUL = "ap_FRONTHAUL6000";
    private static final String NAME_SPACE_FRONTHAUL_AP = "ap_FRONTHAUL-6000";
    private static final String NAME_SPACE_SHARED_CNF_OSS = "ap_Shared-CNF";
    private static final String VALID_NODE_TYPE_FH_AP = "FRONTHAUL-6000";

    private static final String R6K2_NODE_TYPE_IN_OSS = "Router6000-2";

    private static final String NAME_SPACE_ROUTER6000 = "ap_Router6000-2";
    private static final String VALID_NODE_TYPE_SHARED_CNF = "SharedCNF";
    private static final String VALID_NODE_TYPE_SHARED_CNF_OSS = "Shared-CNF";


    @Mock
    private CapabilityInformation capabilityInformation;

    @Mock
    private ModelMetaInformation modelMetaInformation;

    @Mock
    private ModelService modelService;

    @Mock
    private TypedModelAccess typedModelAccess;

    @InjectMocks
    private NodeTypeMapperImpl nodeTypeMapper;

    @Before
    public void setUp() {
        when(modelService.getTypedAccess()).thenReturn(typedModelAccess);
        when(typedModelAccess.getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(modelService.getModelMetaInformation()).thenReturn(modelMetaInformation);
    }

    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ERBS);
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE);
        final String nodeType = nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE);
        assertEquals(VALID_NODE_TYPE.toLowerCase(), internalNodeType);
        assertEquals(VALID_NODE_TYPE, nodeType);

    }

    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeRouterIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ROUTER);
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE_R6K);
        assertEquals(VALID_NODE_TYPE_R6K, internalNodeType);
    }

    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeApRouterIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ROUTER6000);
        final String internalNodeType = nodeTypeMapper.toApRepresentation(R6K2_NODE_TYPE_IN_OSS);
        assertEquals(VALID_NODE_TYPE_R6K, internalNodeType);
    }


    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeFronthaulIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_FRONTHAUL);
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE_FH);
        assertEquals(VALID_NODE_TYPE_FH, internalNodeType);
    }

    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeApFronthaulIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_FRONTHAUL_AP);
        final String internalNodeType = nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE_FH_AP);
        assertEquals(VALID_NODE_TYPE_FH, internalNodeType);
    }


    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeSharedCNFIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_SHAREDCNF);
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE_SHARED_CNF);
        assertEquals(VALID_NODE_TYPE_SHARED_CNF, internalNodeType);
    }

    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceExistsThenInternalNodeTypeApSharedCNFIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_SHARED_CNF_OSS);
        final String internalNodeType = nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE_SHARED_CNF_OSS);
        assertEquals(VALID_NODE_TYPE_SHARED_CNF,internalNodeType);
    }


    @Test
    public void whenGetInternalRepresentationAndNodeTypeNamespaceDoesNotExistThenDefaultNodeTypeIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ECIM);
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE);
        assertEquals(DEFAULT_INTERNAL_NODE_TYPE, internalNodeType);
    }

    @Test
    public void whenGetInternalRepresentationAndExceptionIsThrownRetrievingModelThenDefaultNodeTypeIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        doThrow(UnknownModelException.class).when(capabilityInformation).getCapabilityValue(anyString(), anyString(), anyString(), anyString(),
            anyString());
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE);
        assertEquals(DEFAULT_INTERNAL_NODE_TYPE, internalNodeType);
    }

    @Test
    public void whenGetEjbQualiferAndNodeTypeNamespaceExistsThenInternalNodeTypeIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ERBS);
        final String ejbQualifer = nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE);
        assertEquals(VALID_NODE_TYPE.toLowerCase(), ejbQualifer);
    }

    @Test
    public void whenGetEjbQualiferAndNodeTypeNamespaceDoesNotExistAndVirtualisationIsNotSupportedThenDefaultNodeTypeIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ECIM);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), eq("supportsVirtualization"), anyString()))
            .thenReturn(false);
        final String ejbQualifer = nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE);
        assertEquals(DEFAULT_INTERNAL_NODE_TYPE, ejbQualifer);
    }

    @Test
    public void whenGetEjbQualiferAndNodeTypeNamespaceDoesNotExistAndVirtualisationIsSupportedThenVnfNodeTypeIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ECIM);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), eq("supportsVirtualization"), anyString()))
            .thenReturn(true);
        final String ejbQualifer = nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE);
        assertEquals(VNF_INTERNAL_NODE_TYPE, ejbQualifer);
    }

    @Test
    public void whenGetNamespaceAndNodeTypeNamespaceExistsThenNodeTypeNamespaceIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ERBS);
        final String namespace = nodeTypeMapper.getNamespace(VALID_NODE_TYPE);
        assertEquals(String.format(NODE_TYPE_NAMESPACE_FORMAT, VALID_NODE_TYPE.toLowerCase()), namespace);
    }

    @Test
    public void whenGetNamespaceAndNodeTypeNamespaceDoesNotExistThenDefaultNodeTypeNamespaceIsReturned() {
        when(modelService.getTypedAccess().getModelInformation(CapabilityInformation.class)).thenReturn(capabilityInformation);
        when(capabilityInformation.getCapabilityValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(NAME_SPACE_ECIM);
        final String namespace = nodeTypeMapper.getNamespace(WRONG_NODE_TYPE);
        assertEquals(String.format(NODE_TYPE_NAMESPACE_FORMAT, DEFAULT_INTERNAL_NODE_TYPE), namespace);
    }
}
