/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.xml.exception;

/**
 * Exception thrown when parsing XMLs.
 */
public class XmlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorFileName;

    public XmlException(final String message) {
        super(message);
        errorFileName = "";
    }

    public XmlException(final String message, final Throwable exception) {
        super(message, exception);
        errorFileName = "";
    }

    public XmlException(final String errorFileName, final String message) {
        super(message);
        this.errorFileName = errorFileName;
    }

    /**
     * Get the name of the XML file that has this error.
     *
     * @return the erroneous file's name
     */
    public String getErrorFileName() {
        return errorFileName;
    }
}
