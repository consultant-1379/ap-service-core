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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;
import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.DIRECTORY_LIST;
import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.FILENAME;
import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.FILE_CONTENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;

/**
 * Base class for any validation that is based on a .zip archive.
 */
public abstract class ZipBasedValidation implements ValidationRule {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private NodeInfoReader nodeInfoReader;

    protected final ApMessages apMessages = new ApMessages();

    @Override
    public final boolean execute(final ValidationContext context) {
        return validate(context, getDirectoryList(context));
    }

    public NodeInfo getNodeInfo(final ValidationContext context, final String dirName) {
        final Archive projectArchive = getArchive(context);
        return nodeInfoReader.read(projectArchive, dirName);
    }

    public List<ArchiveArtifact> getArtifactsInDirectory(final ValidationContext context, final String dirName) {
        final Archive projectArchive = getArchive(context);
        return projectArchive.getArtifactsInDirectory(dirName);
    }

    public List<ArchiveArtifact> getArtifactsOfName(final ValidationContext context, final String name) {
        final Archive projectArchive = getArchive(context);
        return projectArchive.getArtifactsOfName(name);
    }

    public String getArtifactOfName(final ValidationContext context, final String name) {
        final Archive projectArchive = getArchive(context);
        return projectArchive.getArtifactContentAsString(name);
    }

    public ArchiveArtifact getArtifactOfNameInDir(final ValidationContext context, final String dirName, final String name) {
        final Archive projectArchive = getArchive(context);
        return projectArchive.getArtifactOfNameInDir(dirName, name);
    }

    @SuppressWarnings("unchecked")
    public String getZipFileName(final ValidationContext context) {
        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();
        return (String) contextTarget.get(FILENAME.toString());
    }

    @SuppressWarnings("unchecked")
    public Archive getArchive(final ValidationContext context) {
        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();
        return (Archive) contextTarget.get(FILE_CONTENT.toString());
    }

    @SuppressWarnings("unchecked")
    public List<String> getDirectoryList(final ValidationContext context) {
        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();
        final Object object = contextTarget.get(DIRECTORY_LIST.toString());
        if(object == null) {
            return Collections.emptyList();
        }

        return (List<String>) contextTarget.get(DIRECTORY_LIST.toString());
    }

    protected final void recordValidationError(final ValidationContext context, final String key, final String... args) {
        final String validationErrorMessage = apMessages.format(key, (Object[]) args);
        context.addValidationError(validationErrorMessage);
    }

    protected final void recordNodeValidationError(final ValidationContext context, final String key, final String directory, final String... args) {
        final String validationErrorMessage = apMessages.format(key, (Object[]) args);
        context.addNodeValidationError(validationErrorMessage, directory);
    }

    //newly added for EarlyBind error message
    protected final void recordNodeValidationErrorBind(final ValidationContext context, final String key, final String... args) {
        final String validationErrorMessage = apMessages.format(key, (Object[]) args);
        logger.error(validationErrorMessage);
        context.addValidationErrorMessage(validationErrorMessage);
    }

    protected boolean isValidatedWithoutError(final ValidationContext context) {
        return context.getValidationErrors().isEmpty();
    }

    protected abstract boolean validate(final ValidationContext context, final List<String> directoryNames);

    protected static List<String> getAllArtifactNames(final NodeInfo nodeInfo) {
        final Collection<List<String>> allArtifactNames = nodeInfo.getNodeArtifacts().values();
        final List<String> artifactNames = new ArrayList<>(allArtifactNames.size());

        for (final List<String> artifactNamesOfType : allArtifactNames) {
            artifactNames.addAll(artifactNamesOfType);
        }
        return artifactNames;
    }

    protected List<String> readArtifactFileNamesInDir(final ValidationContext context, final String dirName) {
        final List<ArchiveArtifact> nodeArtifacts = getArtifactsInDirectory(context, dirName);

        final List<String> artifactNamesInDir = new CaseInsensitiveStringList();
        for (final ArchiveArtifact nodeArtifact : nodeArtifacts) {
            final String fileName = nodeArtifact.getName();
            if (!fileName.equalsIgnoreCase(NODEINFO.toString())) {
                artifactNamesInDir.add(fileName);
            }
        }
        return artifactNamesInDir;
    }

    protected List<String> readArtifactFileNamesInNodeInfo(final ValidationContext context, final String dirName) {
        final NodeInfo nodeInfo = getNodeInfo(context, dirName);
        final List<String> artifactNamesInNodeInfo = new CaseInsensitiveStringList();
        artifactNamesInNodeInfo.addAll(getAllArtifactNames(nodeInfo));
        return artifactNamesInNodeInfo;
    }

    public static class CaseInsensitiveStringList extends ArrayList<String> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(final Object o) {
            final String paramStr = (String) o;
            for (final String s : this) {
                if (paramStr.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }
    }
}
