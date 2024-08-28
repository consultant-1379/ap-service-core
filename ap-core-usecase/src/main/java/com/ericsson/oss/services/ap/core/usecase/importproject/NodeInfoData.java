/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.util.xml.DocumentBuilder.getDocument;
import static com.ericsson.oss.services.ap.common.util.xml.XPaths.getXpathExpression;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Holds all the info needed to correctly describe a nodeInfo.xml file.
 */
public class NodeInfoData {

    private static final XPathExpression XPATH = getXpathExpression("/nodeInfo/name");

    private String nodeName;
    private final Document document;

    public NodeInfoData(final ArchiveArtifact artifact) {
        this(artifact.getContentsAsString());
    }

    public NodeInfoData(final String content) {
        document = getDocument(content);
    }

    public String getNodeName() {
        try {
            if (nodeName == null) {
                nodeName = (String) XPATH.evaluate(document, XPathConstants.STRING);
            }
            return nodeName;
        } catch (final XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }
}