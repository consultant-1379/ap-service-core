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
package com.ericsson.oss.services.ap.api.account;

/**
 * Object representing an <code>AutoProvisioningAccount</code> MO.
 */
public class ApAccount {

    private final String accountType;
    private final String nodeType;
    private final String password;
    private final String userName;
    private String nodeName;

    /**
     * Constructs an instance of {@link ApAccount}.
     *
     * @param accountType
     *            the type of the account (LDAP, SMRS, etc)
     * @param nodeType
     *            the type of the node
     * @param userName
     *            the username of the account
     * @param password
     *            password of the account (in plaintext)
     */
    public ApAccount(final String accountType, final String nodeType, final String userName, final String password) {
        this.accountType = accountType;
        this.nodeType = nodeType;
        this.userName = userName;
        this.password = password;
    }

    public ApAccount(final String accountType, final String nodeType, final String userName, final String password, final String nodeName) {
        this.accountType = accountType;
        this.nodeType = nodeType;
        this.userName = userName;
        this.password = password;
        this.nodeName = nodeName;
    }

    /**
     * The account type.
     *
     * @return the account type
     */
    public final String getAccountType() {
        return accountType;
    }

    /**
     * The node type.
     *
     * @return the node type
     */
    public final String getNodeType() {
        return nodeType;
    }

    /**
     * The account usename.
     *
     * @return the account username
     */
    public final String getUserName() {
        return userName;
    }

    /**
     * The account password in plaintext.
     *
     * @return the account password
     */
    public final String getPassword() {
        return password;
    }

    /**
     * The node name.
     *
     * @return the node name
     */
    public String getNodeName() {
        return nodeName;
    }
}
