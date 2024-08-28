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
package com.ericsson.oss.services.ap.core.metadata.cli.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base interface for metadata types. It's required as some JAXB objects return more than one type in a a {@link java.util.List}. For example
 * {@link ViewItemMetadata#getViewComponentsMetadata()}.
 */
@XmlRootElement
public interface Metadata {

}
