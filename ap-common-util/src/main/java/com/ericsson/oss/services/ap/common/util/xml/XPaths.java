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
package com.ericsson.oss.services.ap.common.util.xml;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Utility class for creating {@link XPathExpression} objects based on an expression.
 */
public final class XPaths {

    private XPaths() {

    }

    /**
     * Creates an {@link XPathExpression} instance for a given expression in string form.
     *
     * @param expression
     *            a valid XPath expression
     * @return the compiled {@link XPathExpression}, which may be used with other Java XML APIs.
     * @throws IllegalArgumentException
     *             if the expression is malformed
     */
    public static XPathExpression getXpathExpression(final String expression) {
        try {
            return XPathFactory.newInstance().newXPath().compile(expression);
        } catch (final XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
