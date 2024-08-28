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
package com.ericsson.oss.services.ap.common.util.xml

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class DocumentReaderSpec extends CdiSpecification{

    private static final String[] ROOT_COMMENTS = [
        " Metadata generatedBy:ECT version:3.45  ",
        " Sample xml "
    ] as String[];

    private static final String XML_CONTENTS = """<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\">"
                    "<managedElementId>LTE01dg2ERBS00001</managedElementId>"
                    "</ManagedElement>""";

    def "when getRootChildComments then comments directly under document are returned"(){
        given: "An valid xml"
            final DocumentReader documentReader = new DocumentReader(readFileOnClasspath("/xml/personInfo.xml"));

        when: "getRootComments is called"
            final List<String> rootComments = documentReader.getRootComments();

        then: "get the root comments correctly"
            assertEquals(ROOT_COMMENTS.length, rootComments.size());
            assertTrue(rootComments.containsAll(Arrays.asList(ROOT_COMMENTS)));
    }

    def "when getMetadataComments then metadata directly under document is returned"(){
        given: "An valid xml"
            final DocumentReader documentReader = new DocumentReader(readFileOnClasspath("/xml/personInfo.xml"));

        when: "getRootComments is called"
            final String metadata = documentReader.getMetadataComment();

        then: "get the metadata correctly"
            assertTrue(ROOT_COMMENTS[0].replace("Metadata", "").equals(metadata));
    }

    def "when getRootTag for a valid xml then root tag is returned"(){
        given: "A valid xml"
            final DocumentReader documentReader = new DocumentReader(XML_CONTENTS);

        when: "getRootTag is called"
            final String rootTag = documentReader.getRootTag();

        then: "the root tag is expected"
            rootTag.equals("ManagedElement");
    }

    private String readFileOnClasspath(final String path) {
        try {
            final InputStream is = this.getClass().getResourceAsStream(path);
            final Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.toString())
            return scanner.useDelimiter("\\A").next();
        } catch (final IOException e) {
            fail("Error reading classpath resource " + path);
        }
        return null;
    }
}
