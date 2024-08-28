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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.PASSWORD;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.USER_NAME;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Creates the {@link MoType#NODE_USER_CREDENTIALS} MO.
 */
public class NodeUserCredentialsMoCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private ModelReader modelReader;

    @Inject
    private CryptographyService cyptographyService;

    @Inject
    private Logger logger;

    /**
     * Write {@link MoType#NODE_USER_CREDENTIALS} data in the AP model.
     *
     * @param nodeMo   the AP <code>Node</code> MO
     * @param nodeInfo the node data from nodeInfo.xml
     * @return the created NodeUserCredentials MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        if (nodeInfo.getNodeUserCredentialAttributes().isEmpty()) {
            return null;
        }
        return createNodeUserCredentialsMo(nodeMo, nodeInfo);
    }

    private ManagedObject createNodeUserCredentialsMo(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String nodeUserCredentialsVersion = getNodeUserCredentialsVersion();
        final Map<String, Object> userCredentialAttributes = getAttributesWithEncryptedPasswords(nodeInfo);
        logger.info("Creating NodeUserCredentials for {}", nodeMo.getName());

        return dps.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(Namespace.AP.toString())
            .version(nodeUserCredentialsVersion)
            .type(MoType.NODE_USER_CREDENTIALS.toString())
            .name("1")
            .addAttributes(userCredentialAttributes)
            .create();
    }

    private String getNodeUserCredentialsVersion() {
        final HierarchicalPrimaryTypeSpecification nodeUserCredentialsModel = modelReader.getLatestPrimaryTypeSpecification(Namespace.AP.toString(),
            MoType.NODE_USER_CREDENTIALS.toString());
        final ModelInfo nodeUserCredentialsModelInfo = nodeUserCredentialsModel.getModelInfo();
        return nodeUserCredentialsModelInfo.getVersion().toString();
    }

    private Map<String, Object> getAttributesWithEncryptedPasswords(final NodeInfo nodeInfo) {
        final Map<String, Object> attributes = nodeInfo.getNodeUserCredentialAttributes();
        encryptPassword(attributes, NodeUserCredentialsAttributes.SECURE_PASSWORD.toString());
        return attributes;
    }

    private void encryptPassword(final Map<String, Object> attributes, final String passwordKey) {
        if (attributes.containsKey(passwordKey)) {
            final String password = (String) attributes.get(passwordKey);
            final byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            final byte[] encryptedPasswordByteArray = cyptographyService.encrypt(passwordBytes);
            final List<Byte> encryptedPasswordByteList = Arrays.asList(ArrayUtils.toObject(encryptedPasswordByteArray));
            attributes.put(passwordKey, encryptedPasswordByteList);
        }
    }


    public ManagedObject eoiCreate(final ManagedObject nodeMo, final Map<String, Object> nodeData) {

        NodeInfo nodeInfo = new NodeInfo();
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), nodeData.get(USER_NAME.toString()));
        attributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), nodeData.get(PASSWORD.toString()));
        nodeInfo.setNodeUserCredentialAttributes(attributes);
        return create(nodeMo,nodeInfo);
    }

}

