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
package com.ericsson.oss.services.ap.common.configuration

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class DirectoryConfigurationSpec extends CdiSpecification {

    def "Verify the exposed locations of Auto Provisioning directories on the ENM filesystem"() {

        when: "the methods returning the absolute path directories are called"
            def artifactsDirectory = DirectoryConfiguration.getArtifactsDirectory()
            def bindDirectory = DirectoryConfiguration.getBindDirectory()
            def cliMetaDataDirectory = DirectoryConfiguration.getCliMetaDataDirectory()
            def downloadDirectory = DirectoryConfiguration.getDownloadDirectory()
            def generatedDirectory = DirectoryConfiguration.getGeneratedDirectory()
            def rawDirectory = DirectoryConfiguration.getRawDirectory()
            def restoreDirectory = DirectoryConfiguration.getRestoreDirectory()
            def projectDirectory = DirectoryConfiguration.getProjectDirectory("project1")
            def nodeDirectory = DirectoryConfiguration.getNodeDirectory("project1","node1")
            def schemasDirectory = DirectoryConfiguration.getSchemasDirectory()
            def samplesDirectory = DirectoryConfiguration.getSamplesDirectory()
            def temporaryDirectory = DirectoryConfiguration.getTemporaryDirectory()
            def profileDirectory = DirectoryConfiguration.getProfileDirectory()

        then: "the expected absolute path directories should be returned"
            artifactsDirectory == "/ericsson/autoprovisioning/artifacts"
            bindDirectory == "/ericsson/autoprovisioning/artifacts/generated/bind"
            cliMetaDataDirectory == "/opt/ericsson/autoprovisioning/metadata/cli"
            downloadDirectory == "/ericsson/autoprovisioning/artifacts/download"
            generatedDirectory == "/ericsson/autoprovisioning/artifacts/generated"
            rawDirectory == "/ericsson/autoprovisioning/artifacts/raw"
            restoreDirectory == "/ericsson/tor/data/enmbur"
            projectDirectory == "/ericsson/autoprovisioning/artifacts/project1"
            nodeDirectory == "/ericsson/autoprovisioning/artifacts/project1/node1"
            schemasDirectory == "/opt/ericsson/autoprovisioning/artifacts/schemadata/schemas"
            samplesDirectory == "/opt/ericsson/autoprovisioning/artifacts/schemadata/samples"
            temporaryDirectory == "/ericsson/autoprovisioning/artifacts/temp"
            profileDirectory == "/ericsson/autoprovisioning/projects"
    }

}
