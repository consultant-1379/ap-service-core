/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.exception.DataPersistenceServiceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;

/**
 * Class used to extract arguments from ENM CLI commands.
 */
class ArgumentResolver {

    private static final String FILE_NAME_IDENTIFIER = "fileName";
    private static final String FILE_PATH_IDENTIFIER = "filePath";

    private ResourceService resourceService;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    @Inject
    private SystemRecorder recorder;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Resolves the FDN of an AP project or node. If command has 'p' option then attempt is made to resolve project FDN. If command has 'n' option
     * then node FDN will be resolved.
     *
     * @param commandOptions
     *            the command options
     * @param commandName
     *            the name of the command
     * @return the FDN of the MO
     * @throws ApServiceException
     *             if there is an error reading the project or node
     * @throws IllegalArgumentException
     *             if there is no project ('p') or node ('n') option in the command options
     * @throws NodeNotFoundException
     *             if no node is found with the given name
     * @throws ProjectNotFoundException
     *             if no project is found with the given name
     */
    public String resolveFdn(final CommandLine commandOptions, final CommandLogName commandName) {
        if (!isProjectSearch(commandOptions) && !isNodeSearch(commandOptions)) {
            throw new IllegalArgumentException("Command does not contain project or node options");
        }
        final String moName = getMoName(commandOptions);
        final String moType = getMoType(commandOptions);
        return findProjectOrNodeFdn(moName, moType, commandName);
    }

    private String findProjectOrNodeFdn(final String moName, final String moType, final CommandLogName commandName) {
        try {
            final Iterator<ManagedObject> nodeMos = dpsQueries.findMoByName(moName, moType, AP.toString()).execute();
            if (!nodeMos.hasNext()) {
                throw moType.equals(MoType.PROJECT.toString()) ? new ProjectNotFoundException(moName) : new NodeNotFoundException(moName);
            }
            return nodeMos.next().getFdn();
        } catch (final DataPersistenceServiceException e) {
            logger.error("Error resolving FDN for {} {}", moType, moName, e);
            recorder.recordError(commandName.toString(), ErrorSeverity.ERROR, moName, "", e.getMessage());
            throw new ApServiceException(String.format("Error resolving FDN for %s %s", moType, moName), e);
        }
    }

    private static boolean isProjectSearch(final CommandLine commandOptions) {
        return commandOptions.hasOption(CliCommandOption.PROJECT.getShortForm());
    }

    private static boolean isNodeSearch(final CommandLine commandOptions) {
        return commandOptions.hasOption(CliCommandOption.NODE.getShortForm());
    }

    private static String getMoType(final CommandLine commandOptions) {
        return isProjectSearch(commandOptions) ? MoType.PROJECT.toString() : MoType.NODE.toString();
    }

    private static String getMoName(final CommandLine commandOptions) {
        return isProjectSearch(commandOptions) ? commandOptions.getOptionValue(CliCommandOption.PROJECT.getShortForm())
                : commandOptions.getOptionValue(CliCommandOption.NODE.getShortForm());
    }

    /**
     * Retrieves the file name from the input properties.
     *
     * @param commandProperties
     *            the ENM CLI command properties
     * @return the file name
     */
    public String getFileName(final Map<String, Object> commandProperties) {
        return (String) commandProperties.get(FILE_NAME_IDENTIFIER);
    }

    /**
     * Retrieves the file content of the input file.
     *
     * @param commandProperties
     *            the ENM CLI command properties
     * @return the file contents
     */
    public byte[] getFileContent(final Map<String, Object> commandProperties) {
        final String filePath = (String) commandProperties.get(FILE_PATH_IDENTIFIER);
        return resourceService.getBytes(filePath);
    }
}
