/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.model.eoi;

public class LdapConfigurationResponse {

    private String tlsPort;
    private String ldapsPort;
    private String ldapIpAddress;
    private String fallbackLdapIpAddress;
    private String bindDn;
    private String bindPassword;
    private String baseDn;

    public String getTlsPort() {
        return tlsPort;
    }

    public void setTlsPort(String tlsPort) {
        this.tlsPort = tlsPort;
    }

    public String getLdapsPort() {
        return ldapsPort;
    }

    public void setLdapsPort(String ldapsPort) {
        this.ldapsPort = ldapsPort;
    }

    public String getLdapIpAddress() {
        return ldapIpAddress;
    }

    public void setLdapIpAddress(String ldapIpAddress) {
        this.ldapIpAddress = ldapIpAddress;
    }

    public String getFallbackLdapIpAddress() {
        return fallbackLdapIpAddress;
    }

    public void setFallbackLdapIpAddress(String fallbackLdapIpAddress) {
        this.fallbackLdapIpAddress = fallbackLdapIpAddress;
    }

    public String getBindDn() {
        return bindDn;
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    @Override
    public String toString() {
        return "LdapConfigurationResponse{" +
                "tlsPort='" + tlsPort + '\'' +
                ", ldapsPort='" + ldapsPort + '\'' +
                ", ldapIpAddress='" + ldapIpAddress + '\'' +
                ", fallbackLdapIpAddress='" + fallbackLdapIpAddress + '\'' +
                ", bindDn='" + bindDn + '\'' +
                ", bindPassword='" + bindPassword + '\'' +
                ", baseDn='" + baseDn + '\'' +
                '}';
    }

}
