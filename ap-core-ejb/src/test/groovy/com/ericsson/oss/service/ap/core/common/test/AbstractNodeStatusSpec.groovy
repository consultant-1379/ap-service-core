/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.service.ap.core.common.test

import javax.inject.Inject

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.status.StatusEntry
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec

abstract class AbstractNodeStatusSpec extends CdiSpecification {

    protected static final String PROJECT_FDN = "Project=Project1"
    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String NODE_NAME = "Node1"

    @Inject
    protected DpsQueries dpsQueries

    @Inject
    protected DataPersistenceService dataPersistenceService

    @Inject
    protected StatusEntryManagerLocal statusEntryManager

    protected RuntimeConfigurableDps dps

    protected ManagedObject projectMo

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        dpsQueries.dps = dataPersistenceService
        MoCreatorSpec.setDps(dps)
        projectMo = MoCreatorSpec.createProjectMo(PROJECT_FDN)
    }

    /**
     * Checks if the size of a list if entries is as expected
     *
     * @param statusEntries
     *          The list of the status entries from a given node
     * @param expectedSize
     *          The expected size of the status entries list
     */
    def assertNoOfStatusEntries(final List<StatusEntry> statusEntries, final int expectedSize) {
        if (expectedSize == 0) {
            statusEntries == null || statusEntries.size() == expectedSize
        } else {
            statusEntries.size() == expectedSize
        }
    }

    /**
     * Checks if every status entry in a list is as expected
     *
     * @param statusEntries
     *          A list of status entries to be verified
     * @param taskName
     *          The expected taskName for the status entries
     * @param taskProgress
     *          The expected taskProgress for the status entries
     * @param additionalInfo
     *          The expected additionalInfo for the status entries
     */
    def assertStatusEntries(final List<StatusEntry> statusEntries, final String taskName, final String taskProgress, final String additionalInfo = null) {
        statusEntries.every() {
            it.taskName == taskName
            it.taskProgress == taskProgress
            it.additionalInfo == additionalInfo
        }
    }

    /**
     * Checks if a given status entry is as expected
     *
     * @param statusEntries
     *          A StatusEntry to be verified
     * @param taskName
     *          The expected taskName for the status entry
     * @param taskProgress
     *          The expected taskProgress for the status entry
     * @param additionalInfo
     *          The expected additionalInfo for the status entry
     */
    def assertStatusEntry(final StatusEntry statusEntry, final String taskName, final String taskProgress, final String additionalInfo = null) {
        statusEntry.taskName == taskName
        statusEntry.taskProgress == taskProgress
        statusEntry.additionalInfo == additionalInfo
    }

    /**
     * Create a node status entry with the given attributes
     *
     * @param taskName
     *          The task name to be added to the status entry
     * @param taskProgress
     *          The progress of state to be added the status entry
     * @param additionalInfo
     *          The additional info to be added to the status entry
     * @return A formatted status entry string
     */
    def createStatusEntry(taskName, taskProgress, additionalInfo = null) {
        return String.format("{\"taskName\":\"%s\",\"taskProgress\":\"%s\",\"timeStamp\":\"Completion Time\",\"additionalInfo\":\"%s\"}", taskName, taskProgress, additionalInfo)
    }
}
