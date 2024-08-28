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

import static com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants.DPS_PRIMARYTYPE;
import static com.ericsson.oss.services.ap.common.model.EnumType.NODETYPE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.ModelServiceImpl;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
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
 * Wrapper class to invoke {@link ModelService} methods to read modelled data.
 */
public class ModelReader {

    private static final String FH_NODE_TYPE_IN_AP = "FRONTHAUL6000";
    private static final String FH_NODE_TYPE_IN_OSS = "FRONTHAUL-6000";
    private static final String R6K2_NODE_TYPE_IN_AP = "Router60002";
    private static final String R6K2_NODE_TYPE_IN_OSS = "Router6000-2";
    private static final Map<List<String>, String> ossModelIdentityCache = new HashMap<>();

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    private ModelService modelService;
    private final Logger logger = LoggerFactory.getLogger(ModelReader.class);

    /**
     * Gets the latest version of the primary type model.
     *
     * @param modelNameSpace
     *            the model namespace
     * @param modelName
     *            the model name
     * @return the latest model
     */
    public ModelData getLatestPrimaryTypeModel(final String modelNameSpace, final String modelName) {
        final ModelInfo modelInfo = getModelService().getModelMetaInformation().getLatestVersionOfModel(SchemaConstants.DPS_PRIMARYTYPE,
            modelNameSpace, modelName);
        return new ModelData(modelInfo.getNamespace(), modelInfo.getVersion().toString());
    }

    /**
     * Gets the <code>HierarchicalPrimaryTypeSpecification</code> for the latest version of the model in the specified namespace.
     *
     * @param namespace
     *            the namespace
     * @param modelName
     *            the name of the model in the namespace
     * @return the {@link HierarchicalPrimaryTypeSpecification} for the specified model in the namespace
     */
    public HierarchicalPrimaryTypeSpecification getLatestPrimaryTypeSpecification(final String namespace, final String modelName) {
        final ModelData latestModel = getLatestPrimaryTypeModel(namespace, modelName);
        return getPrimaryTypeSpecification(latestModel.getNameSpace(), latestModel.getVersion(), modelName);
    }

    /**
     * Gets the <code>HierarchicalPrimaryTypeSpecification</code> for the specified version of the named model.
     *
     * @param namespace
     *            the namespace
     * @param version
     *            the version of the model
     * @param modelName
     *            the name of the model in the namespace
     * @return the {@link HierarchicalPrimaryTypeSpecification} for the specified model version
     */
    public HierarchicalPrimaryTypeSpecification getPrimaryTypeSpecification(final String namespace, final String version, final String modelName) {
        final ModelInfo modelInfo = new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, namespace, modelName, version);
        return getModelService().getTypedAccess().getEModelSpecification(modelInfo, HierarchicalPrimaryTypeSpecification.class);
    }

    /**
     * Gets the <code>EnumDataTypeSpecification</code> for the latest version of the model in the specified namespace and conforming to the specified
     * schema.
     *
     * @param schemaName
     *            the schema name
     * @param namespace
     *            the namespace
     * @param modelName
     *            the name of the model in the namespace
     * @return <code>EnumDataTypeSpecification</code>
     */
    public EnumDataTypeSpecification getLatestEnumDataTypeSpecification(final String schemaName, final String namespace, final String modelName) {
        final ModelInfo modelInfo = getModelService().getModelMetaInformation().getLatestVersionOfModel(schemaName, namespace, modelName);
        return getModelService().getTypedAccess().getEModelSpecification(modelInfo, EnumDataTypeSpecification.class);
    }

    /**
     * Gets the <code>EnumDataTypeSpecification</code> for the specified version of the named model in the specified namespace and conforming to the
     * specified schema.
     *
     * @param schemaName
     *            the schema name
     * @param namespace
     *            the namespace
     * @param modelName
     *            the name of the model in the namespace
     * @param version
     *            the version of the model
     * @return <code>EnumDataTypeSpecification</code>
     */
    public EnumDataTypeSpecification getEnumDataTypeSpecification(final String schemaName, final String namespace, final String modelName,
        final String version) {
        final ModelInfo modelInfo = new ModelInfo(schemaName, namespace, modelName, version);
        return getModelService().getTypedAccess().getEModelSpecification(modelInfo, EnumDataTypeSpecification.class);
    }

    /**
     * Gets the associated ossModelIdentity for the provided node type and node version.
     *
     * @param nodeType
     *            the node type
     * @param nodeVersion
     *            the node version
     * @return ossModelIdentity
     */
    public String getOssModelIdentity(final String nodeType, final String nodeVersion) {
        final TargetTypeVersionInformation targetTypeVersionInformation = getTargetTypeVersionInformation(nodeType);
        final Set<String> ossModelIdentities = targetTypeVersionInformation.getTargetModelIdentities();

        for (final String ossModelIdentity : ossModelIdentities) {
            final Collection<MimMappedTo> mappedMims = targetTypeVersionInformation.getMimsMappedTo(ossModelIdentity);

            if (isNodeVersionInMappedMims(nodeVersion, mappedMims)) {
                return ossModelIdentity;
            }
        }
        throw new ApServiceException(String.format("No OssModelIdentity found for node type %s with node version %s", nodeType, nodeVersion));
    }

    /**
     * Returns an associated {@link String} OSS Model Identity given a Node Type, Product Number and Product Revision.
     * <p>
     * If no associated OSS Model Identity value is found in {@link ModelService} for the provided Node Type, Product Number and Product Revision,
     * then {@code null} is returned.
     * </p>
     *
     * @param nodeType
     *            a Node Type
     * @param productNumber
     *            a Product Number in the format of ENM NE Type Product Identity.
     * @param productRevision
     *            a Product Revision
     * @return a {@link String} OSS Model Identity, if no associated OSS Model Identity is found in {@link ModelService} then {@code null} is
     *         returned.
     */
    public String getOssModelIdentity(final String nodeType, String productNumber, final String productRevision) {

        // This is hack for the Product Number String Mismatch between SHM Returned Value and ENM NE Type Product Identity (returned by Model Service)
        if (productNumber.contains("_")) {
            productNumber = productNumber.replace("_", "/");
        }

        final List<String> attributes = Arrays.asList(nodeType, productNumber, productRevision);
        final List<String> omiKey = Collections.unmodifiableList(attributes);
        if (ossModelIdentityCache.containsKey(omiKey)) {
            return ossModelIdentityCache.get(omiKey);
        }
        final TargetTypeVersionInformation targetTypeVersionInformation = getTargetTypeVersionInformation(nodeType);
        final Set<String> targetModelIdentities = targetTypeVersionInformation.getTargetModelIdentities();
        for (final String targetModelIdentity : targetModelIdentities) {
            final Collection<ProductInfo> productInfos = targetTypeVersionInformation.getProductInfos(targetModelIdentity);
            for (final ProductInfo productInfo : productInfos) {
                for (final ProductData productData : productInfo.getProductData()) {
                    if (productNumber.equals(productData.getIdentity()) && productRevision.equals(productData.getRevision())) {
                        ossModelIdentityCache.put(omiKey, targetModelIdentity);
                        return targetModelIdentity;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if the OSS model identity exists for the given NE type.
     * <p>
     * If not, then the given OSS model identity is not supported for this NE type by the OSS.
     *
     * @param nodeType
     *            the (case sensitive) NetworkElement type
     * @param ossModelIdentity
     *            the OSS model identity
     * @return <code>true</code> if OSS model identiy is available for the given NE type
     * @see TargetTypeVersionInformation
     */
    public boolean checkOssModelIdentityExists(final String nodeType, final String ossModelIdentity) {
        final TargetTypeVersionInformation targetTypeVersionInformation = getTargetTypeVersionInformation(nodeType);
        final Set<String> ossModelIdentities = targetTypeVersionInformation.getTargetModelIdentities();
        return ossModelIdentities.contains(ossModelIdentity);
    }

    /**
     * Get the MIMVersion, corresponding to the given NE type and OSS model identity.
     *
     * @param nodeType
     *            the (case sensitive) NetworkElement type
     * @param ossModelIdentity
     *            the OSS model identity
     * @return the MIM version for the first mapped MIM corresponding to the given NE type and OSS model identity
     * @see TargetTypeVersionInformation
     */
    public String getMimVersionMappedToOssModelIdentity(final String nodeType, final String ossModelIdentity) {
        final TargetTypeVersionInformation targetTypeVersionInformation = getTargetTypeVersionInformation(nodeType);
        final Collection<MimMappedTo> mappedMims = targetTypeVersionInformation.getMimsMappedTo(ossModelIdentity);
        final MimMappedTo mappedMim = mappedMims.iterator().next();
        return mappedMim.getVersion();
    }

    /**
     * Gets the node types currently supported by Auto Provisioning.
     *
     * @return A collection of node type names, excluding any node types that are not supported by AP.
     */
    public Collection<String> getSupportedNodeTypes() {
        final Collection<String> nodeTypes = getTypes(AP.toString(), NODETYPE.toString());
        if (nodeTypes.remove(FH_NODE_TYPE_IN_AP)) {
            nodeTypes.add(FH_NODE_TYPE_IN_OSS);
        }
        if (nodeTypes.remove(R6K2_NODE_TYPE_IN_AP)) {
            nodeTypes.add(R6K2_NODE_TYPE_IN_OSS);
        }
        final Collection<String> neTypes = getTypes(OSS_NE_DEF.toString(), "NeType");
        nodeTypes.retainAll(neTypes);

        return nodeTypes;
    }

    /**
     * Gets the namespace of the ConnectivityInfo for a given node type.
     *
     * @param nodeType
     *            the nodeType to retrieve the ConnectivityInfo for. E.g. Router6672
     * @return Map containing the namespace and type of the ConnectivityInfo for the given node
     */
    public Map<String, Object> getNameSpaceForTargetConnectivityInfo(final String nodeType) {
        final Map<String, Object> ciDetails = new HashMap<>();
        final TargetTypeInformation targetTypeInformation = getModelService().getTypedAccess().getModelInformation(TargetTypeInformation.class);
        final ModelInfo modelInfo = extractLatestConnectivityInformationModelInfo(nodeType, targetTypeInformation);

        if (modelInfo != null) {
            ciDetails.put("namespace", modelInfo.getNamespace());
            ciDetails.put("type", modelInfo.getName());
        }

        return ciDetails;
    }

    /**
     * Indicates if the specified Mo type for Node is system created or not.
     *
     * @param moType
     *            the Name of the Mo model
     * @param nodeType
     *            the node type for the Mo model
     * @param ossModelIdentity
     *            the oss model identity of the Network Element
     * @return boolean true if the Mo model for the specified node type is system created
     */
    public boolean isSystemCreateMo(final String moType, final String nodeType, final String ossModelIdentity) {
        final TargetTypeVersionInformation targetTypeVersionInformation = getTargetTypeVersionInformation(nodeType);
        final Collection<MimMappedTo> mims = targetTypeVersionInformation.getMimsMappedTo(ossModelIdentity);

        final ModelMetaInformation modelMetaInformation = getModelService().getModelMetaInformation();

        for (final MimMappedTo mimMappedTo : mims) {
            try {
                if (modelMetaInformation.isModelDeployed(
                    new ModelInfo(DPS_PRIMARYTYPE, mimMappedTo.getNamespace(), moType, mimMappedTo.getVersion()))) {
                    final ModelInfo modelInfo = modelMetaInformation.getLatestVersionOfModel(DPS_PRIMARYTYPE, mimMappedTo.getNamespace(), moType);
                    final PrimaryTypeSpecification primaryTypeSpec = getModelService().getTypedAccess().getEModelSpecification(modelInfo,
                        PrimaryTypeSpecification.class);
                    return primaryTypeSpec.isSystemCreated();
                }
            } catch (final Exception e) {
                logger.debug(String.format("Could not find model %s with namespace %s", moType, mimMappedTo.getNamespace()));
            }
        }
        return false;
    }

    private ModelInfo extractLatestConnectivityInformationModelInfo(final String nodeType,
        final TargetTypeInformation targetTypeInformation) {
        final ModelInfo connectivityModel = ModelInfo.fromImpliedUrn(targetTypeInformation.getConnectivityInfoMoType("NODE", nodeTypeMapper.toOssRepresentation(nodeType)),
            SchemaConstants.DPS_PRIMARYTYPE);
        ModelInfo latestModelInfo = null;
        if (connectivityModel != null && connectivityModel.getVersion() != null && connectivityModel.getVersion().isSimpleVersion()) {
            if ("*".equals(connectivityModel.getVersion().toString())) {
                latestModelInfo = getModelService().getModelMetaInformation().getLatestVersionOfModel(SchemaConstants.DPS_PRIMARYTYPE,
                    connectivityModel.getNamespace(), connectivityModel.getName());
            }
        } else {
            latestModelInfo = connectivityModel;
        }
        return latestModelInfo;
    }

    private TargetTypeVersionInformation getTargetTypeVersionInformation(final String nodeType) {
        final TargetTypeInformation targetTypeInformation = getModelService().getTypedAccess().getModelInformation(TargetTypeInformation.class);
        return targetTypeInformation.getTargetTypeVersionInformation(TargetTypeInformation.CATEGORY_NODE, nodeTypeMapper.toOssRepresentation(nodeType));
    }

    private static boolean isNodeVersionInMappedMims(final String nodeVersion, final Collection<MimMappedTo> mappedMims) {
        for (final MimMappedTo mim : mappedMims) {
            if (mim.getVersion().equals(nodeVersion)) {
                return true;
            }
        }
        return false;
    }

    private ModelService getModelService() {
        if (modelService == null) {
            modelService = new ModelServiceImpl();
        }
        return modelService;
    }

    private Collection<String> getTypes(final String namespace, final String modelName) {
        return getLatestEnumDataTypeSpecification(SchemaConstants.OSS_EDT, namespace, modelName).getMemberNames();
    }
}
