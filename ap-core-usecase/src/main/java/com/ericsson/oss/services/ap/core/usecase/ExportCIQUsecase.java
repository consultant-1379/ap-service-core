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
package com.ericsson.oss.services.ap.core.usecase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Generates a CIQ file from a given Profile and then downloads that file to the user.
 */
@UseCase(name = UseCaseName.EXPORT_CIQ)
public class ExportCIQUsecase extends BaseProfileUseCase {

    private ResourceService resourceService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * @param projectFdn The fdn of the given Project
     * @param profileId  The id of the Profile
     * @return a unique file id for the file to be exported.
     * @throws ProfileNotFoundException if no Profile found.
     * @throws ApApplicationException if error exporting CIQ File.
     */
    public String execute(final String projectFdn, final String profileId) {
        try {
            return generateDownloadFile(projectFdn, profileId);
        } catch (final ProfileNotFoundException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new ApApplicationException(String.format("Error exporting CIQ for profile %s", profileId),
                exception);
        }
    }

    private String generateDownloadFile(final String projectFdn, final String profileId) {
        final Iterator<ManagedObject> profileMos = getAllProfileMOs(projectFdn);
        final String profileFdn = String.format("%s,ConfigurationProfile=%s", projectFdn, profileId);

        checkIfProfileExists(profileFdn);

        final Map<String, Object> attributes = profileMos.next().getAllAttributes();
        final Map<String, Object> ciq = (Map<String, Object>) attributes.get(ProfileAttribute.CIQ.toString());
        final String ciqLocation = (String) ciq.get(ProfileAttribute.CIQ_LOCATION.toString());

        verifyDownloadDirAccess(ciqLocation);

        byte[] ciqFile = null;
        String fileName = "";
        final List<Resource> ciqFiles = (List<Resource>) resourceService.listFiles(ciqLocation);
        if (null == ciqFiles || ciqFiles.isEmpty()) {
            throw new ApApplicationException(String.format("No CIQ found to export for Profile %s", profileId));
        }

        for (final Resource resource : ciqFiles) {
            fileName = resource.getName();
            // Don't call getBytes from the Resource directly because it leads to connection leak.
            ciqFile = resourceService.getBytes(String.format("%s/%s", ciqLocation, fileName));
        }

        final String fileExtension = fileName.split("[.]")[1];
        final String projectName = projectFdn.split("=")[1];
        final String uniqueFileId = new StringBuilder()
            .append("CIQ")
            .append("_")
            .append(projectName)
            .append("_")
            .append(profileId)
            .append("_")
            .append(createTimeStamp())
            .append(".")
            .append(fileExtension)
            .toString();

        final String fileUri = DirectoryConfiguration.getDownloadDirectory() + File.separator + uniqueFileId;
        resourceService.write(fileUri, ciqFile, false);
        return uniqueFileId;
    }

    private void verifyDownloadDirAccess(final String ciqDirectory) {
        if (!resourceService.supportsWriteOperations(ciqDirectory)) {
            throw new ApApplicationException("Unable to access system resource " + ciqDirectory);
        }
    }

    private static String createTimeStamp() {
        final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmm");
        return timeStampFormat.format(new Date());
    }
}
