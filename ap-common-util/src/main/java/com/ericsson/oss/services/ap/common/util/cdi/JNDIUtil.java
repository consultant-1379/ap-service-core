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
package com.ericsson.oss.services.ap.common.util.cdi;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utility to perform JNDI lookup.
 */
public class JNDIUtil {

    private static final Properties LOOKUP_PROPERTIES = new Properties();

    static {
        LOOKUP_PROPERTIES.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        LOOKUP_PROPERTIES.put("jboss.naming.client.ejb.context", "true");
    }

    /**
     * Performs jndi lookup for the specifed name.
     *
     * @param jndiName
     *            the JNDI name to lookup
     * @return the named object
     * @throws NamingException
     *             if a naming exception is encountered
     */
    @SuppressWarnings("unchecked")
    public <T> T doLookup(final String jndiName) throws NamingException {
        final InitialContext initialCtx = new InitialContext(LOOKUP_PROPERTIES);
        return (T) initialCtx.lookup(jndiName);
    }

}
