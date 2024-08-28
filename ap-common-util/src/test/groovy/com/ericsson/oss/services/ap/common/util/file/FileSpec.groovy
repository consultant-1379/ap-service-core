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
package com.ericsson.oss.services.ap.common.util.file

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class FileSpec extends CdiSpecification{

    private final String fileName = "ProjectInfo.xsd"
    private final String fileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "    \n" +
            "    <xs:complexType name=\"ProjectContent\">\n" +
            "        <xs:sequence>\n" +
            "            <xs:element name=\"name\" type=\"ValidName\"/>\n" +
            "            <xs:element name=\"description\" minOccurs=\"0\" type=\"xs:string\"/>\n" +
            "            <xs:element name=\"creator\" type=\"xs:string\"/>\n" +
            "        </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "\n" +
            "    <xs:simpleType name=\"ValidName\">\n" +
            "        <xs:restriction base=\"xs:string\">\n" +
            "            <xs:pattern value=\"([a-zA-Z0-9._-])*\"/> <!-- Only alphanumeric characters and special characters dot(.), dash(-) and " +
            "underscore(_) are allowed. -->\n" +
            "            <xs:minLength value=\"3\"/>\n" +
            "            <xs:maxLength value=\"200\"/>\n" +
            "        </xs:restriction>\n" +
            "    </xs:simpleType>\n" +
            "\n" +
            "    <!-- Elements of ProjectInfo -->\n" +
            "    <xs:element name=\"projectInfo\" type=\"ProjectContent\"/>\n" +
            "</xs:schema>"

    def "When Map of String, Object is given a File is returned with correct the name and content"(){
        given: "A Map of String, Object"
            final Map<String, Object> fileMap = new HashMap<>( name: fileName, content: fileContent)

        when:"fromMap is called"
            final File file = File.fromMap(fileMap)

        then: "The returned File will contain the correct name and content"
            file.getName() == fileName
            file.getContent() == fileContent
    }

    def "When a list of Map<String, Object> is given a list of files with correct values is returned"(){
        given:"A List of String, Object maps"
            final String fileName2 = "AnotherProjectInfo.xsd"
            final List<Map<String, Object>> listOfMaps = new ArrayList<Map<String, Object>>()
            listOfMaps.add(new HashMap<>( name : fileName , content : fileContent))
            listOfMaps.add(new HashMap<>( name : fileName2 , content : fileContent))

        when:"fromMapList is called"
            final List<File> listOfFiles = File.fromMapList(listOfMaps)

        then:"The returned list of files will contain the correct values"
            listOfFiles.size() == 2
            listOfFiles.get(0).getName() == fileName
            listOfFiles.get(0).getContent() == fileContent
            listOfFiles.get(1).getName() == fileName2
            listOfFiles.get(1).getContent() == fileContent
    }

    def "When a File is given a Map of String, Object is returned"(){
        given:"A File object with the name and content set"
            final File testFile = new File(fileName, fileContent)

        when:"toMap is called"
            final Map<String, Object> fileMap = File.toMap(testFile)

        then:"The returned Map will contain the correct name and content"
            fileMap.get("name") == fileName
            fileMap.get("content") == fileContent
    }
}
