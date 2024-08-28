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
package com.ericsson.oss.services.ap.common.util.xml.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

/**
 * Used for reading schemas.
 */
public class XsdInput implements LSInput {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String publicId;
    private String systemId;

    private BufferedInputStream inputStream;

    public XsdInput(final String publicId, final String sysId, final InputStream input) {
        this.publicId = publicId;
        systemId = sysId;
        inputStream = new BufferedInputStream(input);
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public void setPublicId(final String publicId) {
        this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public InputStream getByteStream() {
        return null;
    }

    @Override
    public boolean getCertifiedText() { // NOPMD PMD requesting isCertifiedText(). This is impossible as we are overriding a method
        return false;
    }

    @Override
    public Reader getCharacterStream() {
        return null;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public String getStringData() {
        synchronized (inputStream) {
            try {
                final StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
                return writer.toString();
            } catch (final IOException e) {
                logger.debug("Error reading input: {}", e.getMessage(), e);
                return null;
            }
        }
    }

    @Override
    public void setBaseURI(final String baseUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteStream(final InputStream byteStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCertifiedText(final boolean certifiedText) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(final Reader characterStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEncoding(final String encoding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStringData(final String stringData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public void setSystemId(final String systemId) {
        this.systemId = systemId;
    }

    public BufferedInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(final BufferedInputStream inputStream) {
        this.inputStream = inputStream;
    }

}