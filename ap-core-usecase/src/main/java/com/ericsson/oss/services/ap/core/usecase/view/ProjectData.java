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
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.view;

import static com.ericsson.oss.services.ap.common.model.MoType.CONFIGURATION_PROFILE;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.node.Node;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;

/**
 * DTO class enabling transfer from {@link ManagedObject} to {@link MoData} for project MO
 */
public final class ProjectData {

    private ProjectData() {
    }

    /**
     * Transfers data from {@link ManagedObject} to {@link MoData} in context of project MO
     *
     * @param projectMo
     *            {@link ManagedObject} to transfer to {@link MoData}
     * @return {@link MoData} representing transferred data
     */
    public static MoData createProjectData(final ManagedObject projectMo) {
        final Collection<ManagedObject> managedObjects = projectMo.getChildren();
        final List<ManagedObject> nodeMos = new ArrayList<>();
        int nodeQuantity = 0;
        for (final ManagedObject managedObject : managedObjects) {
            if (managedObject.getType().equals(NODE.toString())) {
                nodeMos.add(managedObject);
                nodeQuantity++;
            }
        }
        final Map<String, Object> projectAttributes = new LinkedHashMap<>();

        projectAttributes.put("projectName", projectMo.getName());
        projectAttributes.put("nodeQuantity", String.valueOf(nodeQuantity));
        projectAttributes.put("nodes", buildNodeObject(nodeMos));
        projectAttributes.putAll(new TreeMap<>(projectMo.getAllAttributes()));
        readProfileIdInformation(managedObjects, projectAttributes);
        return new MoData(projectMo.getFdn(), projectAttributes, projectMo.getType(),
            new ModelData(projectMo.getNamespace(), projectMo.getVersion()));
    }

    private static void readProfileIdInformation(Collection<ManagedObject> managedObjects, Map<String, Object> projectAttributes) {
        for (final ManagedObject managedObject : managedObjects) {
            if (managedObject.getType().equals(CONFIGURATION_PROFILE.toString())) {
                final ManagedObject profileManagedObject = managedObject;
                getProfileNamesByType(profileManagedObject, projectAttributes);
            }
        }
    }

    private static void getProfileNamesByType(final ManagedObject profileManagedObject, Map<String, Object> projectAttributes) {
        final String profileType = profileManagedObject.getAttribute(ProfileAttribute.DATATYPE.toString());
        final String profileName = profileManagedObject.getAttribute(ProfileAttribute.PROFILE_ID.toString());
        if (profileType == null || profileType.equals("INTEGRATION")) {
            projectAttributes.put("integrationProfile", profileName);
        } else {
            projectAttributes.put("expansionProfile", profileName);
        }
    }

    private static List<Node> buildNodeObject(final Collection<ManagedObject> nodeMos) {

        final List<Node> nodes = new ArrayList<>(nodeMos.size());
        nodeMos.forEach(node -> nodes.add(new Node(
            node.getName(),
            node.getAttribute(NodeAttribute.NODE_TYPE.toString()),
            node.getAttribute(NodeAttribute.NODE_IDENTIFIER.toString()),
            node.getAttribute(NodeAttribute.IPADDRESS.toString()),
            node.getAttribute(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString()),
            node.getAttribute(NodeAttribute.WORK_ORDER_ID.toString()),
            node.getParent().getName())));
        return nodes;
    }
}
