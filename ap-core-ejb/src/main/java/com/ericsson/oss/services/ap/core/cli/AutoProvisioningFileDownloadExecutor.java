/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.activation.MimetypesFileTypeMap;
import javax.ejb.Stateless;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceQualifier;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.core.cli.handlers.DownloadCommandHandler;
import com.ericsson.oss.services.scriptengine.spi.FileDownloadHandler;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileSystemLocatedFileDto;

/**
 * Handles file download request callback from script-engine.
 * <p>
 * Invoked as a result of a response from {@link DownloadCommandHandler} which requested a file to be downloaded.
 */
@Stateless
@EServiceQualifier("ap")
public class AutoProvisioningFileDownloadExecutor implements FileDownloadHandler {

    @EServiceRef
    private ResourceService resourceService;

    private final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    @Override
    public FileDownloadResponseDto execute(final String fileId) {
        final String downloadDir = DirectoryConfiguration.getDownloadDirectory();
        final String fileDownloadPath = downloadDir + File.separator + fileId;

        if (!resourceService.exists(fileDownloadPath)) {
            throw new IllegalStateException("File " + fileId + " does not exist in the download staging area. Please retry the download command");
        }

        final Path downloadDirPath = Paths.get(downloadDir);
        final Path downloadFilePath = downloadDirPath.resolve(fileId);
        final String fileName = fileId.substring(fileId.indexOf('_') + 1);
        return new FileSystemLocatedFileDto(downloadFilePath.toString(), fileName,
                mimetypesFileTypeMap.getContentType(fileId));
    }
}
