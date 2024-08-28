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
package com.ericsson.oss.services.ap.common.model.access;

import static com.ericsson.oss.services.ap.common.model.EnumType.NODETYPE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.TypedModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.MimMappedTo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.ProductData;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.ProductInfo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeVersionInformation;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;

/**
 * Unit tests for {@link ModelReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelReaderTest {

    private static final String VALID_NODE_TYPE = "ERBS";
    private static final String OSS_MODEL_IDENTITY = "5504-866-139";
    private static final String MAPPED_MIM_VERSION = "6.1.20";
    private static final String ER_6000_MED = "ER6000_MED";
    private static final String ROUTER_6672 = "Router6672";
    private static final String ER6000_CONNECTIVITY_INFORMATION = "Er6000ConnectivityInformation";
    private static final String DPS_PRIMARYTYPE = "dps_primarytype";
    private static final String PRODUCT_NUMBER = "CXP9024418/5";
    private static final String PRODUCT_REVISION = "R2CXS";

    @Mock
    private ModelService modelService;

    @Mock
    private TypedModelAccess typeModelAccess;

    @Mock
    private HierarchicalPrimaryTypeSpecification pts;

    @Mock
    private EnumDataTypeSpecification enumDataTypeSpec;

    @Mock
    private ModelMetaInformation modelMeta;

    @Mock
    private TargetTypeInformation targetTypeInformation;

    @Mock
    private TargetTypeVersionInformation targetTypeVersionInformation;

    @Mock
    private ProductInfo productInfo;

    @Mock
    private ProductData productData;

    @Mock
    private MimMappedTo mappedMim;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    private final ModelReader modelReader = new ModelReader();

    @Before
    public void setup() {
        final Set<String> ossModelIdentites = new HashSet<>();
        ossModelIdentites.add(OSS_MODEL_IDENTITY);

        final Set<ProductInfo> productInfos = new HashSet<>();
        productInfos.add(productInfo);

        final Set<ProductData> productDatas = new HashSet<>();
        productDatas.add(productData);

        when(mappedMim.getVersion()).thenReturn(MAPPED_MIM_VERSION);
        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);
        final Collection<MimMappedTo> mappedMims = new ArrayList<>();
        mappedMims.add(mappedMim);

        when(modelService.getTypedAccess()).thenReturn(typeModelAccess);
        when(typeModelAccess.getModelInformation(TargetTypeInformation.class)).thenReturn(targetTypeInformation);
        when(targetTypeInformation.getTargetTypeVersionInformation(TargetTypeInformation.CATEGORY_NODE, VALID_NODE_TYPE))
            .thenReturn(targetTypeVersionInformation);
        when(targetTypeVersionInformation.getTargetModelIdentities()).thenReturn(ossModelIdentites);
        when(targetTypeVersionInformation.getProductInfos(OSS_MODEL_IDENTITY)).thenReturn(productInfos);
        when(productInfo.getProductData()).thenReturn(productDatas);
        when(productData.getIdentity()).thenReturn(PRODUCT_NUMBER);
        when(productData.getRevision()).thenReturn(PRODUCT_REVISION);
        when(targetTypeVersionInformation.getMimsMappedTo(OSS_MODEL_IDENTITY)).thenReturn(mappedMims);
    }

    @Test
    public void whenGetLatestPrimaryTypeSpecificationThePtsIsReturnedForTheLatestVersionOfTheNamedModel() {
        final ModelInfo modelInfo = new ModelInfo(DPS_PRIMARYTYPE, "OSS_TOP", "MeContext", "1.0.0");

        when(modelService.getModelMetaInformation()).thenReturn(modelMeta);
        when(modelMeta.getLatestVersionOfModel(SchemaConstants.DPS_PRIMARYTYPE, "OSS_TOP", "MeContext")).thenReturn(modelInfo);
        when(modelService.getTypedAccess()).thenReturn(typeModelAccess);
        when(typeModelAccess.getEModelSpecification(modelInfo, HierarchicalPrimaryTypeSpecification.class)).thenReturn(pts);

        final HierarchicalPrimaryTypeSpecification actualPts = modelReader.getLatestPrimaryTypeSpecification("OSS_TOP", "MeContext");
        assertEquals(pts, actualPts);
    }

    @Test
    public void whenGetNamespaceForTargetConnectivityInfoForThenNamespaceAndTypeReturned() {
        final ModelInfo modelInfo = new ModelInfo(DPS_PRIMARYTYPE, ER_6000_MED, ER6000_CONNECTIVITY_INFORMATION, "1.0.0");
        when(nodeTypeMapper.toOssRepresentation(ROUTER_6672)).thenReturn(ROUTER_6672);
        when(targetTypeInformation.getConnectivityInfoMoType("NODE", ROUTER_6672)).thenReturn("//ER6000_MED/Er6000ConnectivityInformation/*");
        when(modelService.getModelMetaInformation()).thenReturn(modelMeta);
        when(modelMeta.getLatestVersionOfModel(DPS_PRIMARYTYPE, ER_6000_MED, ER6000_CONNECTIVITY_INFORMATION)).thenReturn(modelInfo);
        final Map<String, Object> ciInfo = modelReader.getNameSpaceForTargetConnectivityInfo(ROUTER_6672);

        assertEquals(ciInfo.get("namespace"), ER_6000_MED);
        assertEquals(ciInfo.get("type"), ER6000_CONNECTIVITY_INFORMATION);
    }

    @Test
    public void whenGetLatestPrimaryTypeModelTheModelInfoIsReturnedForTheLatestVersionOfTheNamedModel() {
        final ModelData expectedModelData = new ModelData("ERBS_NODE_MODEL", "1.0.0");
        final ModelInfo modelInfo = new ModelInfo(DPS_PRIMARYTYPE, "ERBS_NODE_MODEL", "ENodeBFunction", "1.0.0");

        when(modelService.getModelMetaInformation()).thenReturn(modelMeta);
        when(modelMeta.getLatestVersionOfModel(SchemaConstants.DPS_PRIMARYTYPE, "ERBS_NODE_MODEL", "ENodeBFunction")).thenReturn(modelInfo);

        final ModelData result = modelReader.getLatestPrimaryTypeModel("ERBS_NODE_MODEL", "ENodeBFunction");
        assertEquals(expectedModelData, result);
    }

    @Test
    public void whenGetLatestEnumDataTypeSpecificationTheEnumDataTypeSpecificationIsReturnedForLatestVersionOfTheNamedModel() {
        final ModelInfo modelInfo = new ModelInfo(SchemaConstants.OSS_EDT, AP.toString(), NODETYPE.toString(), "2.0.0");
        when(modelService.getModelMetaInformation()).thenReturn(modelMeta);
        when(modelMeta.getLatestVersionOfModel(SchemaConstants.OSS_EDT, AP.toString(), NODETYPE.toString())).thenReturn(modelInfo);
        when(modelService.getTypedAccess()).thenReturn(typeModelAccess);
        when(typeModelAccess.getEModelSpecification(modelInfo, EnumDataTypeSpecification.class)).thenReturn(enumDataTypeSpec);

        final EnumDataTypeSpecification actualEnumSpec = modelReader.getLatestEnumDataTypeSpecification(SchemaConstants.OSS_EDT, AP.toString(),
            NODETYPE.toString());

        assertEquals(enumDataTypeSpec, actualEnumSpec);
    }

    @Test
    public void whenGetEnumDataTypeSpecificationTheEnumDataTypeSpecificationIsReturnedForSpecifiedVersionOfTheNamedModel() {
        final ModelInfo modelInfo = new ModelInfo(SchemaConstants.OSS_EDT, AP.toString(), NODETYPE.toString(), "2.0.0");

        when(modelService.getTypedAccess()).thenReturn(typeModelAccess);
        when(typeModelAccess.getEModelSpecification(modelInfo, EnumDataTypeSpecification.class)).thenReturn(enumDataTypeSpec);

        final EnumDataTypeSpecification actualEnumSpec = modelReader.getEnumDataTypeSpecification(SchemaConstants.OSS_EDT, AP.toString(),
            NODETYPE.toString(), "2.0.0");

        assertEquals(enumDataTypeSpec, actualEnumSpec);
    }

    @Test
    public void testCheckOssModelIdentityExistsValid() {
        assertTrue(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, OSS_MODEL_IDENTITY));
    }

    @Test
    public void testCheckOssModelIdentityExistsInValid() {
        assertFalse(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, MAPPED_MIM_VERSION));
    }

    @Test
    public void testOssModelIdentityIsRetrievedFromMappedMims() {
        final String ossModelIdentity = modelReader.getOssModelIdentity(VALID_NODE_TYPE, MAPPED_MIM_VERSION);
        assertEquals(OSS_MODEL_IDENTITY, ossModelIdentity);
    }

    @Test(expected = ApServiceException.class)
    public void testRollbackTriggeredIfNoOssModelIdentityFoundThatMapsToNodeVersion() {
        modelReader.getOssModelIdentity(VALID_NODE_TYPE, "6.1.60");
    }

    @Test
    public void ossModelIdentityIsRetrievedFromModelServiceGivenNodeTypeProductNumberAndProductRevision() {
        final String ossModelIdentity = modelReader.getOssModelIdentity(VALID_NODE_TYPE, PRODUCT_NUMBER, PRODUCT_REVISION);
        assertEquals(OSS_MODEL_IDENTITY, ossModelIdentity);
    }

    @Test
    public void noOssModelIdentityIsRetrievedFromModelServiceGivenNodeTypeInvalidProductRevisionAndInvalidProductNumber() {
        final String ossModelIdentity = modelReader.getOssModelIdentity(VALID_NODE_TYPE, "invalidProductNumber", "invalidProductRevision");
        assertNull(ossModelIdentity);
    }
}
