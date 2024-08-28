/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.flowautomation;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

/*
 * Container object for the request body to execute the Apply AMOS Flow.
 */
public class AmosExecuteRequestBody implements Serializable {

    private static final long serialVersionUID = 183479562316123945L;

    private String nodeName;
    private String amosScriptName;
    private String amosScriptContents;
    private String token;
    private Boolean ignoreError;

    private static final String KEY_ALGORITHM = "AES";

    /**
     * Constructor for AmosExecuteRequestBody.
     *
     * @param nodeName
     *            the node name
     * @param amosScriptName
     *            amos script file name
     * @param amosScriptContents
     *            the contents of the amos script
     */
    public AmosExecuteRequestBody(final String nodeName, final String amosScriptName, final String amosScriptContents, final Boolean ignoreError) {
        this.nodeName = nodeName;
        this.amosScriptName = amosScriptName;
        this.amosScriptContents = amosScriptContents;
        this.ignoreError = ignoreError;
    }

    /**
     * Set node name
     *
     * @param nodeName
     *            the node name to be set
     */
    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Get node name
     *
     * @return node name
     *
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Set amos script name
     *
     * @param amosScriptName
     *            the amos script name to be set
     */
    public void setAmosScriptName(final String amosScriptName) {
        this.amosScriptName = amosScriptName;
    }

    /**
     * Get amos script name
     *
     * @return amos script name
     */
    public String getAmosScriptName() {
        return amosScriptName;
    }

    /**
     * Set amos script contents
     *
     * @param amosScriptContents
     *            the amos script contents to be set
     */
    public void setAmosScriptContents(final String amosScriptContents) {
        this.amosScriptContents = amosScriptContents;
    }

    /**
     * Get amos script contents
     *
     * @return the amos script contents
     */
    public String getAmosScriptContents() {
        return amosScriptContents;
    }

    /**
     * if ignore Error for script execution
     *
     * @return indicator
     */
    public Boolean isIgnoreError() {
        return ignoreError;
    }

    /**
     * Set ignoreError
     *
     * @param ignoreError
     *            the indicator to be set
     */
    public void setIgnoreError(final Boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    /**
     * Get token
     *
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * Generate and set token
     *
     * @throws Exception
     *          Exception throw from Security and Crypto SDK
     */
    public synchronized void generateToken() throws Exception {
        final String hashKey = generateHashCode();
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(hashKey.getBytes(StandardCharsets.UTF_8));
        final KeyGenerator keygen = KeyGenerator.getInstance(KEY_ALGORITHM);
        keygen.init(128, random);
        final SecretKey aeskey = new SecretKeySpec(keygen.generateKey().getEncoded(), KEY_ALGORITHM);
        final Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, aeskey);
        final byte[] encryptData = cipher.doFinal(hashKey.getBytes(StandardCharsets.UTF_8));

        this.token = DatatypeConverter.printBase64Binary(encryptData);
    }

    private String generateHashCode() {
        final int prime = 31;
        int result = nodeName.hashCode();
        final String content = (amosScriptContents == null ? "" : amosScriptContents);
        result = prime * result + amosScriptName.hashCode();
        result = prime * result + content.hashCode();
        return StringUtils.leftPad(Integer.toHexString(result), 8, '0');
    }
}
