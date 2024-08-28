/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase

import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean

class DumpSnapshotUseCaseSpec extends CdiSpecification {

    private static final String PROJECT_NAME = "project1"
    private static final String PROFILE_NAME = "profile1"
    private static final String PROFILE_FDN = "Project=project1,ConfigurationProfile=profile1"
    private static final String NODE_NAME = "NODE1"
    private static final String MANAGED_ELEMENT_FDN = "ManagedElement=" + NODE_NAME;
    private static final String NODE_TYPE = "RadioNode"
    private static final Long DEFAULT_DUMP_TIMESTAMP = Long.valueOf(0)

    @ObjectUnderTest
    private DumpSnapshotUseCase dumpSnapshotUsecase

    @Inject
    private DpsQueries dpsQueries

    @Inject
    protected DpsOperations dpsOperations;

    RuntimeConfigurableDps dps

    @MockedImplementation
    private EventSender<MediationTaskRequest> eventSender;

    @MockedImplementation
    private ResourceService resourceService

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(ResourceService.class, null) >> resourceService
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())

        dumpSnapshotUsecase.retryManager = new RetryManagerBean()
    }

    def "Trigger dumping node configuration snapshot successfully" (){
        given: "Required MOs are created"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            createProfileMo(projectMO, PROFILE_NAME)
            addManagedElementMo(NODE_NAME)
            dumpSnapshotUsecase.init()

        when: "usecase is executed"
            eventSender.send(_) >> null
            dumpSnapshotUsecase.execute(PROJECT_NAME, PROFILE_NAME, NODE_NAME, PROFILE_FDN)

        then: "netconf event is sent"
            1 * eventSender.send(_) >> null
            notThrown(Exception)
    }

    def "Trigger dumping node configuration snapshot exception when fail to create timestamp" (){
        given: "Required MOs are created"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "IN_PROGRESS")
            createProfileMoWithProfileAttribute(projectMO, PROFILE_NAME, profileAttributes)
            addManagedElementMo(NODE_NAME)
            dumpSnapshotUsecase.init()

        when: "usecase is executed"
            eventSender.send(_) >> null
            dumpSnapshotUsecase.execute(PROJECT_NAME, PROFILE_NAME, NODE_NAME, PROFILE_FDN)

        then: "netconf event is sent"
            thrown(ApApplicationException.class)
    }

    def "Delete existing snapshot file and trigger dumping node configuration snapshot successfully" (){
        given: "Required MOs are created"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            createProfileMo(projectMO, PROFILE_NAME)
            addManagedElementMo(NODE_NAME)
            dumpSnapshotUsecase.init()

        when: "usecase is executed"
            resourceService.exists(_) >> true
            resourceService.delete(_) >> true
            resourceService.isDirectoryExists(_) >> true
            resourceService.deleteDirectoryIfEmpty(_) >> true
            eventSender.send(_) >> null
            dumpSnapshotUsecase.execute(PROJECT_NAME, PROFILE_NAME, NODE_NAME, PROFILE_FDN)

        then: "netconf event is sent"
            thrown(ApApplicationException.class)
    }

    def "Profile does not exist exception when triggering to dump node configuration snapshot"() {
        given: "Required MOs are created except profile MO"
            addProjectMo("Project="+PROJECT_NAME)
            addManagedElementMo(NODE_NAME)
            dumpSnapshotUsecase.init()

        when: "usecase is executed"
            eventSender.send(_) >> null
            dumpSnapshotUsecase.execute(PROJECT_NAME, PROFILE_NAME, NODE_NAME, PROFILE_FDN)

        then: "the profile not found exception is thrown"
            thrown(ProfileNotFoundException.class)
    }

    def "Ap applicationException exception when triggering to dump node configuration snapshot"() {
        given: "Required MOs are created except ME MO"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            createProfileMo(projectMO, PROFILE_NAME)
            dumpSnapshotUsecase.init()

        when: "usecase is executed"
            eventSender.send(_) >> null
            dumpSnapshotUsecase.execute(PROJECT_NAME, PROFILE_NAME, NODE_NAME, PROFILE_FDN)

        then: "the ap application exception is thrown"
            thrown(ApApplicationException.class)
    }

    private ManagedObject addProjectMo(final String fdn) {
        final Map<String, Object> projectAttributes = new HashMap<String, Object>()
        return dps.addManagedObject()
                        .withFdn(fdn)
                        .type(PROJECT.toString())
                        .namespace(AP.toString())
                        .version("1.0.0")
                        .addAttributes(projectAttributes)
                        .build()
    }

    private ManagedObject createProfileMo(final ManagedObject parentMo, final String profileName) {
        final Map<String, Object> profileAttributes = new HashMap<>()
        profileAttributes.put("profileId", profileName)
        profileAttributes.put("configSnapshotStatus", "NOT_STARTED")
        profileAttributes.put("dumpTimeStamp", DEFAULT_DUMP_TIMESTAMP)

        return dps.addManagedObject()
                        .withFdn("Project=" + parentMo.getName() + ",ConfigurationProfile=" + profileName)
                        .type("ConfigurationProfile")
                        .version("2.0.0")
                        .parent(parentMo)
                        .name(profileName)
                        .addAttributes(profileAttributes)
                        .build()
    }

    private ManagedObject createProfileMoWithProfileAttribute(final ManagedObject parentMo, final String profileName, final Map<String, Object> profileAttributes) {
        return dps.addManagedObject()
                        .withFdn("Project=" + parentMo.getName() + ",ConfigurationProfile=" + profileName)
                        .type("ConfigurationProfile")
                        .version("2.0.0")
                        .parent(parentMo)
                        .name(profileName)
                        .addAttributes(profileAttributes)
                        .build()
    }

    private void addManagedElementMo(final String nodeName) {
        dps.addManagedObject()
            .withFdn("ManagedElement=" + nodeName)
            .type("ManagedElement")
            .namespace("ECIM_Top")
            .version("1.0.0")
            .build();
    }
}
