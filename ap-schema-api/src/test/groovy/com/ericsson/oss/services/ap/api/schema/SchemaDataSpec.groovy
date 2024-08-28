/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 -----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.schema

import spock.lang.Shared
import spock.lang.Specification

/**
 * Unit tests for {@link SchemaData}
 */
class SchemaDataSpec extends Specification {

    private static final byte[] SCHEMA_CONTENT = [0, 0, 1, 1]
    private static final String SCHEMA_TYPE = "SCHEMA"
    private static final String SCHEMA_IDENTIFIER = "v1.1"
    private static final String SCHEMA_ARTIFACT_LOCATION = "/schema/some_schema.xml"

    @Shared SchemaData schemaData

    def "The get methods return the correct information when called"() {
        when:
            schemaData = new SchemaData("name.xml",SCHEMA_TYPE,SCHEMA_IDENTIFIER,SCHEMA_CONTENT,SCHEMA_ARTIFACT_LOCATION)

        then:
            schemaData.getName() == "name"
            schemaData.getExtension() == "xml"
            schemaData.getType() == SCHEMA_TYPE
            schemaData.getData() == SCHEMA_CONTENT
            schemaData.getIdentifier() == SCHEMA_IDENTIFIER
            schemaData.getArtifactLocation() == SCHEMA_ARTIFACT_LOCATION
    }

    def "when SchemaData is created and 'name' has no extension, then the name is returned without a file extension and extension is null"() {
        when:
            schemaData = new SchemaData("name",SCHEMA_TYPE,SCHEMA_IDENTIFIER,SCHEMA_CONTENT,SCHEMA_ARTIFACT_LOCATION)

        then: "The file name is returned without the extension"
            schemaData.getName() == "name"
        and: "null is returned for the file extension"
            schemaData.getExtension() == null
    }

    def "When SchemaData is created, and 'name' or 'data' is null or empty, then an IllegalArgumentException is thrown"() {
        when:
            schemaData = new SchemaData(name,SCHEMA_TYPE,SCHEMA_IDENTIFIER,data,SCHEMA_ARTIFACT_LOCATION)

        then:
            thrown(IllegalArgumentException)

        where:
            name << [null, "", "name", "name"]
            data << [SCHEMA_CONTENT, SCHEMA_CONTENT, null, new byte[0]]
    }
}
