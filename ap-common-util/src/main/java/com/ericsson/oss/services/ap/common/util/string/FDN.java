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
package com.ericsson.oss.services.ap.common.util.string;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * FDN utility class.
 */
public class FDN {

    private static final Pattern FDN_PATTERN = Pattern.compile("^[^,=]+=[^,=]+(,([^,=]+=[^,=]+))*$");

    private final String inputFdn;

    private static final String NETWORK_ELEMENT = "NetworkElement";

    private static final String MECONTEXT = "MeContext";

    private static final String MANAGED_ELEMENT = "ManagedElement";

    private static final String EQUAL_SEPARATOR = "=";

    private static final String COMMA_SEPARATOR = ",";

    public FDN(final String fdn) {
        if (!isValidFdn(fdn)) {
            throw new IllegalArgumentException(String.format("Illegal FDN: [%s]", fdn));
        }
        inputFdn = fdn.trim();
    }

    /**
     * Return an instance of an FDN object from the input FDN string.
     *
     * @param fdn
     *            the FDN to retrieve
     * @return the FDN object
     */
    public static final FDN get(final String fdn) {
        return new FDN(fdn);
    }

    /**
     * Return the MO type.
     * <p>
     * <b>FDN:</b> Project=projectName,Node=nodeName,NodeArtifactContainer=1
     * <p>
     * <b>Type:</b> NodeArtifactContainer
     *
     * @return the type of the MO
     */
    public String getType() {
        return getRdn().split("=")[0];
    }

    /**
     * Return the RDN of the FDN.
     * <p>
     * <b>FDN:</b> Project=projectName,Node=nodeName,NodeArtifactContainer=1
     * <p>
     * <b>RDN:</b> NodeArtifactContainer=1
     *
     * @return the RDN of the MO
     */
    public String getRdn() {
        final String[] tokens = inputFdn.split(",");
        return tokens[tokens.length - 1];
    }

    /**
     * Return the value of the RDN of the FDN.
     * <p>
     * <b>FDN:</b> Project=projectName,Node=nodeName,NodeArtifactContainer=1
     * <p>
     * <b>RDN Value:</b> 1
     *
     * @return the RDN value of the MO
     */
    public String getRdnValue() {
        return getRdn().split("=")[1];
    }

    /**
     * Gets the RDN value for the specified type.
     *
     * @param type
     *            the MO type
     * @return the RDN value or null if FDN does not contain the given type
     */
    public String getRdnValueOfType(final String type) {
        final String[] rdns = inputFdn.split(",");
        for (final String rdn : rdns) {
            if (rdn.startsWith(type)) {
                return rdn.split("=")[1];
            }
        }
        return null;
    }

    /**
     * Return the parent FDN of the FDN.
     * <p>
     * <b>FDN:</b> Project=projectName,Node=nodeName,NodeArtifactContainer=1
     * <p>
     * <b>Parent:</b> Project=projectName,Node=nodeName
     *
     * @return the RDN value of the MO
     */
    public String getParent() {
        final String[] tokens = inputFdn.split(",");

        if (tokens.length == 1) {
            return null;
        }

        final StringBuilder parent = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            parent.append(tokens[i]);
            parent.append(',');
        }
        parent.deleteCharAt(parent.lastIndexOf(","));

        return parent.toString();
    }

    /**
     * Return the root FDN of the FDN.
     * <p>
     * <b>FDN:</b> Project=projectName,Node=nodeName,NodeArtifactContainer=1
     * <p>
     * <b>Root:</b> Project=projectName
     *
     * @return the RDN value of the MO
     */
    public String getRoot() {
        return inputFdn.split(",")[0];
    }

    @Override
    public String toString() {
        return inputFdn;
    }

    private static boolean isValidFdn(final String fdn) {
        return StringUtils.isNotBlank(fdn) && FDN_PATTERN.matcher(fdn).find();
    }

    public String getNodeName() {
        final String[] rdnsArray = inputFdn.split(COMMA_SEPARATOR);
        for (final String rdn : rdnsArray) {
            if (rdn.startsWith(NETWORK_ELEMENT) || rdn.startsWith(MECONTEXT) || rdn.startsWith(MANAGED_ELEMENT)) {
                return rdn.split(EQUAL_SEPARATOR)[1].trim();
            }
        }
        return null;
    }

    public String getProjectName() {
        return inputFdn.split(",")[0].split("=")[1];
    }

}
