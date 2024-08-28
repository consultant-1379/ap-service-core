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
package com.ericsson.oss.service.ap.core

import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.GEN_LOCATION
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.RAW_LOCATION

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.ArtifactLocationUpdaterEjb

class ArtifactLocationUpdaterEjbSpec extends AbstractNodeStatusSpec {

    @ObjectUnderTest
    private ArtifactLocationUpdaterEjb artifactLocationUpdaterEjb

    private static final String OLD_ROOT_ARTIFACT_LOCATION = "/ericsson/tor/data/autoprovisioning/artifacts"
    private static final String NEW_ROOT_ARTIFACT_LOCATION = "/ericsson/autoprovisioning/artifacts"

    def "Update node artifact MO for generated and raw directory locations"  () {

        given: "there are three node artifacts containing the old, new, and empty generated and raw directories"
            ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)

            final def oldArtifactAttributes = [
                    (GEN_LOCATION.toString()): OLD_ROOT_ARTIFACT_LOCATION + "/generated/files/test1.xml",
                    (RAW_LOCATION.toString()): OLD_ROOT_ARTIFACT_LOCATION + "/raw/files/test2.xml"
            ]
            final ManagedObject oldArtifactMo = MoCreatorSpec.createNodeArtifactMo(NODE_FDN + ",NodeArtifactContainer=1,NodeArtifact=1", nodeMo, oldArtifactAttributes)

            final def newArtifactAttributes = [
                    (GEN_LOCATION.toString()): NEW_ROOT_ARTIFACT_LOCATION + "/generated/files/test3.xml",
                    (RAW_LOCATION.toString()): NEW_ROOT_ARTIFACT_LOCATION + "/raw/files/test4.xml"
            ]
            final ManagedObject newArtifactMo = MoCreatorSpec.createNodeArtifactMo(NODE_FDN + ",NodeArtifactContainer=2NodeArtifact=2", nodeMo, newArtifactAttributes)

            final def emptyArtifactAttributes = [(GEN_LOCATION.toString()): "", (RAW_LOCATION.toString()): null]
            final ManagedObject emptyArtifactMo = MoCreatorSpec.createNodeArtifactMo(NODE_FDN + ",NodeArtifactContainer=3NodeArtifact=3", nodeMo, emptyArtifactAttributes)

        when: "the EJB is called to update the artifacts"
            artifactLocationUpdaterEjb.executeUpdate()

        then: "only the generated and raw locations matching the old directory should be updated"
            oldArtifactMo.getAttribute(GEN_LOCATION.toString()) == NEW_ROOT_ARTIFACT_LOCATION + "/generated/files/test1.xml"
            oldArtifactMo.getAttribute(RAW_LOCATION.toString()) == NEW_ROOT_ARTIFACT_LOCATION + "/raw/files/test2.xml"
            newArtifactMo.getAttribute(GEN_LOCATION.toString()) == NEW_ROOT_ARTIFACT_LOCATION + "/generated/files/test3.xml"
            newArtifactMo.getAttribute(RAW_LOCATION.toString()) == NEW_ROOT_ARTIFACT_LOCATION + "/raw/files/test4.xml"
            emptyArtifactMo.getAttribute(GEN_LOCATION.toString()) == ""
            emptyArtifactMo.getAttribute(RAW_LOCATION.toString()) == null
    }

}
