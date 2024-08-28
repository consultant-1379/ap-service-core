/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.model.ProjectAttribute.GENERATED_BY;
import static com.ericsson.oss.services.ap.common.model.ProjectAttribute.NAME;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.access.ModeledAttributeFilter;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Reads data from projectInfo.xml in project archive.
 */
public class ProjectInfoReader {

    private static final String VALIDATION_PROJECTINFO_XML_PARSE_ERROR = "validation.xml.parse.projectinfo";
    private static final String GENERATED_BY_IN_SCHEMA = "generatedBy";

    private final ApMessages apMessages = new ApMessages();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ModeledAttributeFilter modeledAttrFilter;

    /**
     * Reads all elements from projectInfo.xml.
     *
     * @param projectArchiveReader
     *            the project archive
     * @return {@link ProjectInfo}
     */
    public ProjectInfo read(final Archive projectArchiveReader) {
        final List<ArchiveArtifact> projectInfoArtifacts = projectArchiveReader.getArtifactsByPattern(PROJECTINFO.artifactName());
        final String projectInfoXml = projectInfoArtifacts.get(0).getContentsAsString();
        final DocumentReader projectInfoDocument = getProjectInfoDocument(projectInfoXml);

        final ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setName(projectInfoDocument.getElementValue(NAME.toString()));

        final Map<String, String> rootChildElements = projectInfoDocument.getRootChildElementsAsMap();
        setGeneratedbyAttribute(rootChildElements, projectInfoDocument);
        projectInfo.setProjectAttributes(modeledAttrFilter.apply(Namespace.AP.toString(), MoType.PROJECT.toString(), rootChildElements));
        projectInfo.setNodeQuantity(projectArchiveReader.getArtifactsOfName(NODEINFO.artifactName()).size());

        return projectInfo;
    }

    private DocumentReader getProjectInfoDocument(final String projectInfoXml) {
        try {
            return new DocumentReader(projectInfoXml);
        } catch (final XmlException e) {
            logger.warn("Error parsing {} artifact: {}", PROJECTINFO, e.getMessage(), e);
            throw new ValidationException(Arrays.asList("Error parsing projectInfo.xml. " + e.getMessage()),
                    apMessages.get(VALIDATION_PROJECTINFO_XML_PARSE_ERROR));
        }
    }

    private void setGeneratedbyAttribute(final Map<String, String> rootChildElements, final DocumentReader projectInfoDocument) {
        String generateBy = rootChildElements.get(GENERATED_BY_IN_SCHEMA);
        if (StringUtils.isEmpty(generateBy)) {
            generateBy = Arrays.stream(projectInfoDocument.getMetadataComment().split(","))
                    .filter(field -> field.contains(":"))
                    .map(field -> field.split(":", 2))
                    .collect(Collectors.toMap(field -> field[0].trim(), field -> field[1].trim(), (v1, v2)-> v2))
                    .get(GENERATED_BY_IN_SCHEMA);
        }

        if (StringUtils.isNotEmpty(generateBy)) {
            rootChildElements.put(GENERATED_BY.toString(), generateBy);
        }
    }
}
