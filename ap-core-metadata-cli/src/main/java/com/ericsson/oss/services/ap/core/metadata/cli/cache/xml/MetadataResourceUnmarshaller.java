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
package com.ericsson.oss.services.ap.core.metadata.cli.cache.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;

/**
 * Unmarshals XML files, from a given directory, into metadata objects using JAXB.
 */
public class MetadataResourceUnmarshaller {

    private ResourceService resourceService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private SystemRecorder recorder;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Creates all the metadata objects based on the XML files found in the given directory
     *
     * @param dirPath
     *            the directory containing all the XML files to unmarshal
     * @param documentClass
     *            the objects that the XML will be unmarshaled to
     * @return the metadata objects
     */
    public Collection<CliViews> loadMetadata(final String dirPath, final Class<CliViews> documentClass) {
        return buildAllCliModels(dirPath, documentClass);
    }

    private Collection<CliViews> buildAllCliModels(final String directoryPath, final Class<CliViews> documentClass) {
        final Collection<Resource> resources = resourceService.listFiles(directoryPath);
        final Collection<CliViews> cliViewMetadataBind = new ArrayList<>(resources.size());

        for (final Resource resource : resources) {
            final CliViews cliViewMetadata = unmarshalXml(resource, documentClass);

            if (cliViewMetadata != null) {
                cliViewMetadataBind.add(cliViewMetadata);
            }
        }

        return cliViewMetadataBind;
    }

    private <T> T unmarshalXml(final Resource resource, final Class<T> documentClass) {
        try {
            return unmarshal(documentClass, resource);
        } catch (final JAXBException | IOException e) {
            logger.error("Unable to unmarshal view metadata XML", e);
            recorder.recordError(CommandLogName.VIEW.toString(), ErrorSeverity.ERROR, resource.getName(), "", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(final Class<T> documentClass, final Resource resource) throws IOException, JAXBException {
        final String packageName = documentClass.getPackage().getName();
        final JAXBContext jaxbContext = JAXBContext.newInstance(packageName);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        try (final InputStream resourceInputStream = resource.getInputStream()) {
            return (T) unmarshaller.unmarshal(resourceInputStream);
        }
    }
}
