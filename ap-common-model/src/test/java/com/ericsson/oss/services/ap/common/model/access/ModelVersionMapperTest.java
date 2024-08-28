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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.typed.TypedModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.MimMappedTo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeVersionInformation;

/**
 * Unit tests for {@link ModelVersionMapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelVersionMapperTest {

    private static final String ENM_MIM_VERSION = "6.1.108";
    private static final String MODEL_NAME = "ERBS_NODE_MODEL";
    private static final String NODE_MIM_VERSION = "f.1.108";
    private static final String TARGET_MODEL_IDENTITY = "target";
    private static final String VALID_NODE_TYPE = "ERBS";

    @Mock
    private MimMappedTo mappedMim;

    @Mock
    private ModelService modelService;

    @Mock
    private TargetTypeInformation targetTypeInformation;

    @Mock
    private TargetTypeVersionInformation targetTypeVersionInformation;

    @Mock
    private TypedModelAccess typedModelAccess;

    @InjectMocks
    private ModelVersionMapper modelVersionMapper;

    @Before
    public void setUp() {
        when(modelService.getTypedAccess()).thenReturn(typedModelAccess);
        when(typedModelAccess.getModelInformation(TargetTypeInformation.class)).thenReturn(targetTypeInformation);
        when(targetTypeInformation.getTargetTypeVersionInformation(TargetTypeInformation.CATEGORY_NODE, VALID_NODE_TYPE))
            .thenReturn(targetTypeVersionInformation);

        final Set<String> targetModelIdentities = new HashSet<>();
        targetModelIdentities.add(TARGET_MODEL_IDENTITY);
        when(targetTypeVersionInformation.getTargetModelIdentities()).thenReturn(targetModelIdentities);
    }

    @Test
    public void whenCheckingIfNodeVersionIsValidAndVersionExistsThenTrueIsReturned() {
        final Collection<MimMappedTo> mappedMims = new ArrayList<>();
        mappedMims.add(mappedMim);
        when(targetTypeVersionInformation.getMimsMappedTo(TARGET_MODEL_IDENTITY)).thenReturn(mappedMims);
        when(mappedMim.getVersion()).thenReturn(ENM_MIM_VERSION);

        final boolean result = modelVersionMapper.isNodeVersionValid(MODEL_NAME, VALID_NODE_TYPE, NODE_MIM_VERSION);

        assertTrue(result);
    }

    @Test
    public void whenCheckingIfNodeVersionIsValidAndMappedMimDoesNotExistThenFalseIsReturned() {
        final Collection<MimMappedTo> mappedMims = new ArrayList<>();
        mappedMims.add(mappedMim);
        when(targetTypeVersionInformation.getMimsMappedTo(TARGET_MODEL_IDENTITY)).thenReturn(mappedMims);
        when(mappedMim.getVersion()).thenReturn("someOtherMimVersion");

        final boolean result = modelVersionMapper.isNodeVersionValid(MODEL_NAME, VALID_NODE_TYPE, NODE_MIM_VERSION);

        assertFalse(result);
    }

    @Test
    public void whenCheckingIfNodeVersionIsValidAndVersionDoesNotExistThenFalseIsReturned() {
        final Collection<MimMappedTo> mappedMims = new ArrayList<>();
        when(targetTypeVersionInformation.getMimsMappedTo(TARGET_MODEL_IDENTITY)).thenReturn(mappedMims);

        final boolean result = modelVersionMapper.isNodeVersionValid(MODEL_NAME, VALID_NODE_TYPE, NODE_MIM_VERSION);

        assertFalse(result);
    }

    @Test
    public void whenCheckingIfNodeVersionIsValidAndNodeHasNoMatchingModelIdentitiesThenFalseIsReturned() {
        when(targetTypeVersionInformation.getTargetModelIdentities()).thenReturn(Collections.<String> emptySet());
        final boolean result = modelVersionMapper.isNodeVersionValid(MODEL_NAME, VALID_NODE_TYPE, NODE_MIM_VERSION);
        assertFalse(result);
    }
}
