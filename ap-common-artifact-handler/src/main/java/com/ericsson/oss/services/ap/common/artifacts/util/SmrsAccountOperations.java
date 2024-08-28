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
package com.ericsson.oss.services.ap.common.artifacts.util;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.smrs.SmrsAccount;
import com.ericsson.oss.itpf.smrs.SmrsService;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;

/**
 * Common set of operations for managing SMRS node accounts.
 */
public class SmrsAccountOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SmrsService smrsService;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @PostConstruct
    public void init() {
        smrsService = new ServiceFinderBean().find(SmrsService.class);
    }

    /**
     * Gets the AI {@link SmrsAccount} for the node.
     *
     * @param nodeName
     *            the name of the AP node
     * @param nodeType
     *            the type of the node
     * @return the SMRS account
     */
    public SmrsAccount getSmrsAccount(final String nodeName, final String nodeType) {
        try {
            return smrsService.getNodeSpecificAccount("AI", nodeTypeMapper.toOssRepresentation(nodeType), nodeName);
        } catch (final Exception e) {
            throw new ApServiceException("Failed to get SMRS account for node " + nodeName, e);
        }
    }

    /**
     * Deletes the AI {@link SmrsAccount} for the node.
     *
     * @param nodeName
     *            the name of the AP node
     * @param nodeType
     *            the type of the node
     */
    public void deleteSmrsAccount(final String nodeName, final String nodeType) {
        try {
            final SmrsAccount smrsAccount = getSmrsAccount(nodeName, nodeTypeMapper.toOssRepresentation(nodeType));
            smrsService.deleteSmrsAccount(smrsAccount);
        } catch (final Exception e) {
            logger.warn("Failed to delete SMRS account for node {}", nodeName, e);
        }
    }
}
