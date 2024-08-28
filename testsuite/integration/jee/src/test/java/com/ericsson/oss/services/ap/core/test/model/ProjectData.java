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
package com.ericsson.oss.services.ap.core.test.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.arquillian.util.data.project.model.NodeBuilder;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Project;

public class ProjectData {

    private final String nodeType;
    private final String fileName;

    private final List<String> excludedFiles = new ArrayList<>();
    private final Map<String, String> includedFiles = new HashMap<>();

    private String projectName;
    private String[] rawNodeArtifactsSubdirectories;
    private boolean brokenZip;
    private int nodeCount;

    private NodeBuilder nodeBuilder;
    private Project project;

    public ProjectData(final String fileName, final String nodeType) {
        this.fileName = fileName;
        this.nodeType = nodeType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String[] getRawNodeArtifactsSubdirectories() {
        return rawNodeArtifactsSubdirectories;
    }

    public boolean isBrokenZip() {
        return brokenZip;
    }

    public void setBrokenZip(final boolean brokenZip) {
        this.brokenZip = brokenZip;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(final int count) {
        nodeCount = count;
        rawNodeArtifactsSubdirectories = new String[count];
    }

    public NodeBuilder getNodeBuilder() {
        return nodeBuilder;
    }

    public void setNodeBuilder(final NodeBuilder nodeBuilder) {
        this.nodeBuilder = nodeBuilder;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public List<String> getExcludedFiles() {
        return excludedFiles;
    }

    public void addExcludedFile(final String fileName) {
        excludedFiles.add(fileName);
    }

    public Map<String, String> getIncludedFiles() {
        return includedFiles;
    }

    public void addIncludedFile(final String fileName, final String content) {
        includedFiles.put(fileName, content);
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getFileName() {
        return fileName;
    }
}
