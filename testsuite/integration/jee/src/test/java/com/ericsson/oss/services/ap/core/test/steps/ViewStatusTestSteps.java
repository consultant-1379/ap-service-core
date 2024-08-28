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
package com.ericsson.oss.services.ap.core.test.steps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.arquillian.util.Dps;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * All the steps needed for the view status test cases.
 */
public class ViewStatusTestSteps extends ServiceCoreTestSteps {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    private final static String STATUS_ENTRIES_ATTRIBUTE = "statusEntries";
    private final static String STATUS_ENTRY_FORMAT = "{\"taskName\":\"%s\",\"taskProgress\":\"%s\",\"timeStamp\":\"%s\",\"additionalInfo\":\"%s\"}";

    @Inject
    private Dps dps;

    @Step("View status for single project ({0})")
    public ApNodeGroupStatus view_status_for_single_project(final String projectFdn) {
        return service.statusProject(projectFdn);
    }

    @Step("View status for node {0}")
    public NodeStatus view_status_for_node(final String nodeFdn) {
        return service.statusNode(nodeFdn);
    }

    @Step("View status for deployment {0}")
    public ApNodeGroupStatus view_status_for_deployment(final String deployment) {
        return service.statusDeployment(deployment);
    }

    @Step("Set workflow ID for node {0}")
    public void update_workflow_id(final String nodeFdn, final List<String> workflowIdList) {
        final Map<String, Object> nodeAttrs = new HashMap<>();
        nodeAttrs.put("workflowInstanceIdList", workflowIdList);
        dps.updateMo(nodeFdn, nodeAttrs);
    }

    @SuppressWarnings("unchecked")
    @Step("Add new status entry to {0} - {1}, at step {2}")
    public String add_new_status_entry(
            final String nodeFdn,
            final String statusEntryName,
            final String statusEntryProgress,
            final String additionalInfo) {

        final String timeStamp = DATE_FORMAT.get().format(new Date());
        final String nodeStatusFdn = new StringBuilder(nodeFdn).append(",NodeStatus=1").toString();

        final Map<String, Object> nodeStatusAttributes = new HashMap<>();
        final List<String> statusEntries = (List<String>) dps.findMoByFdn(nodeStatusFdn).getAttribute(STATUS_ENTRIES_ATTRIBUTE);
        statusEntries.add(String.format(STATUS_ENTRY_FORMAT, statusEntryName, statusEntryProgress, timeStamp, additionalInfo));
        nodeStatusAttributes.put(STATUS_ENTRIES_ATTRIBUTE, statusEntries);

        dps.updateMo(nodeStatusFdn, nodeStatusAttributes);
        return timeStamp;
    }
}
