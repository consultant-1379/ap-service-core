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
package com.ericsson.oss.services.ap.core.rest.handlers;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.Iterator;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.exception.DataPersistenceServiceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Class used to resolve data sent from the AP UI.
 */
public class ArgumentResolver {

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    @Inject
    private SystemRecorder recorder;

    /**
     * Resolves the FDN of an AP node.
     *
     * @param fdn the fdn of the AP node
     * @param usecase the name of the usecase
     *
     * @throws ApServiceException
     *                  if there is an error reading the project or node
     * @throws ProjectNotFoundException
     *                  if no project is found with the given name
     * @throws NodeNotFoundException
     *                  if no node is found with the given name
     */
    public void resolveFdn(final String fdn, final String usecase) {
        final String moName = getMoName(fdn);
        final String moType = getMoType(fdn);
        findProjectOrNodeFdn(fdn, moName, moType, usecase);
    }

    private void findProjectOrNodeFdn(final String fdn, final String moName, final String moType, final String usecaseName) {
        try {
            final Iterator<ManagedObject> nodeMos = dpsQueries.findMoByName(moName, moType, AP.toString()).execute();
            if (!nodeMos.hasNext()) {
                throw moType.equals(MoType.PROJECT.toString()) ? new ProjectNotFoundException(moName) : new NodeNotFoundException(moName);
            } else if (!nodeMos.next().getFdn().equals(fdn)) {
                throw new ProjectNotFoundException(FDN.get(fdn).getRdnValueOfType(MoType.PROJECT.toString()));
            }
        } catch (final DataPersistenceServiceException e) {
            logger.error("Error resolving FDN for {} {}", moType, moName, e);
            recorder.recordError(usecaseName, ErrorSeverity.ERROR, moName, "", e.getMessage());
            throw new ApServiceException(String.format("Error resolving FDN for %s %s", moType, moName), e);
        }

    }

    private static String getMoName(final String fdn) {
        return isProject(fdn) ? FDN.get(fdn).getRdnValue() : FDN.get(fdn).getRdnValueOfType(MoType.NODE.toString());
    }

    private static String getMoType(final String fdn) {
        return isProject(fdn) ? MoType.PROJECT.toString() : MoType.NODE.toString();
    }

    private static boolean isProject(final String fdn) {
        return null == FDN.get(fdn).getRdnValueOfType(MoType.NODE.toString()) ? Boolean.TRUE : Boolean.FALSE;
    }

}
