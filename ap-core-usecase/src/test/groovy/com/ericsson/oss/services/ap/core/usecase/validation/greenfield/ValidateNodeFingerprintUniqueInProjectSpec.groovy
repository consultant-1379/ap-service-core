/*
 ------------------------------------------------------------------------------
  *******************************************************************************
  * COPYRIGHT Ericsson 2020
  *
  * The copyright to the computer program(s) herein is the property of
  * Ericsson Inc. The programs may be used and/or copied only with written
  * permission from Ericsson Inc. or in accordance with the terms and
  * conditions stipulated in the agreement/contract under which the
  * program(s) have been supplied.
  *******************************************************************************
  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.ap.core.usecase.validation.greenfield

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader

import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.DIRECTORY_LIST
import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.FILENAME
import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.FILE_CONTENT

class ValidateNodeFingerprintUniqueInProjectSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @ObjectUnderTest
    private ValidateNodeFingerprintUniqueInProject validateNodeFingerprintUniqueInProject

    ValidationContext validationContext = createValidationContext()

    def createValidationContext() {
        def projectDataContentTarget = HashMap.newInstance(
                (FILENAME.toString()): "testProject.zip",
                (FILE_CONTENT.toString()): archive,
                (DIRECTORY_LIST.toString()): ["dir1", "dir2", "dir3"])
        new ValidationContext("import", projectDataContentTarget)
    }

    def "Validation fails if a node fingerprint is not unique within an AP Project"() {
        given: "NodeInfoReader returns NodeInfo objects with fingerprint values of fingerprint1, fingerprint2 and fingerprint3"
        setupNodeInfoReaderResponse(fingerprint1, fingerprint2, fingerprint3)

        expect: "Validation fails if fingerprints are not unique and passes if fingerprints are unique"
        passesValidation == validateNodeFingerprintUniqueInProject.execute(validationContext)

        where:
        fingerprint1   | fingerprint2   | fingerprint3   || passesValidation
        "NodeName1"    | "Fingerprint1" | "NodeName2"    || true
        "Fingerprint1" | "Fingerprint1" | "NodeName1"    || false
        null           | null           | "Fingerprint1" || true
    }


    def setupNodeInfoReaderResponse(fingerprint1, fingerprint2, fingerprint3) {
        nodeInfoReader.read(*_) >>> [
                NodeInfo.newInstance(licenseAttributes: ["fingerprint": fingerprint1]),
                NodeInfo.newInstance(licenseAttributes: ["fingerprint": fingerprint2]),
                NodeInfo.newInstance(licenseAttributes: ["fingerprint": fingerprint3])
        ]
    }
}
