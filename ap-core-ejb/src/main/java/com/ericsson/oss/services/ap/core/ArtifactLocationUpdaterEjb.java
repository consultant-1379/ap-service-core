/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import static java.util.concurrent.TimeUnit.MINUTES;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.GEN_LOCATION;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.RAW_LOCATION;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;


/**
 * Handles updating the artifacts location from the old SFS directory to the new dedicated AP directory.
 * This update is due to AP no longer using the <i>/ericsson/tor/data/autoprovisioning/</i> SFS directory.
 */
@EService
@Startup
@Singleton
public class ArtifactLocationUpdaterEjb {

    private static final long INITIAL_EXPIRY = MINUTES.toMillis(5L);

    private static final String OLD_ROOT_ARTIFACT_LOCATION_REGEX = "^(/ericsson/tor/data/autoprovisioning/artifacts";
    private static final String OLD_GENERATED_LOCATION_REGEX = OLD_ROOT_ARTIFACT_LOCATION_REGEX + "/generated)(.*)";
    private static final String OLD_RAW_LOCATION_REGEX = OLD_ROOT_ARTIFACT_LOCATION_REGEX + "/raw)(.*)";

    private static final Pattern OLD_GENERATED_LOCATION_PATTERN = Pattern.compile(OLD_GENERATED_LOCATION_REGEX);
    private static final Pattern OLD_RAW_LOCATION_PATTERN = Pattern.compile(OLD_RAW_LOCATION_REGEX);

    @Inject
    private DpsOperations dps;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        timerService.createSingleActionTimer(INITIAL_EXPIRY, new TimerConfig());
    }

    @Timeout
    public void executeUpdate() {
        updateNodeArtifactGeneratedLocations();
        updateNodeArtifactRawLocations();
    }

    private void updateNodeArtifactGeneratedLocations() {
        Iterator<ManagedObject> generatedLocationMos = dpsQueries.findMosWithAttribute(GEN_LOCATION.toString(), AP.toString(),
            MoType.NODE_ARTIFACT.toString()).execute();

        generatedLocationMos.forEachRemaining(generatedLocationMo -> {
            final String oldGeneratedLocation = generatedLocationMo.getAttribute(GEN_LOCATION.toString());
            if (!StringUtils.isBlank(oldGeneratedLocation)) {
                final Matcher oldGeneratedLocationMatcher = OLD_GENERATED_LOCATION_PATTERN.matcher(oldGeneratedLocation);
                if (oldGeneratedLocationMatcher.find()) {
                    final String newGeneratedLocation = DirectoryConfiguration.getGeneratedDirectory() + oldGeneratedLocationMatcher.group(2);
                    logger.info("Updating node artifact MO generated location from: {}, to: {}", oldGeneratedLocation, newGeneratedLocation);
                    dps.getDataPersistenceService()
                        .getLiveBucket()
                        .findMoByFdn(generatedLocationMo.getFdn())
                        .setAttribute(GEN_LOCATION.toString(), newGeneratedLocation);
                }
            }
        });
    }

    private void updateNodeArtifactRawLocations() {
        Iterator<ManagedObject> rawLocationMos = dpsQueries.findMosWithAttribute(RAW_LOCATION.toString(), AP.toString(),
            MoType.NODE_ARTIFACT.toString()).execute();

        rawLocationMos.forEachRemaining(rawLocationMo -> {
            final String oldRawLocation = rawLocationMo.getAttribute(RAW_LOCATION.toString());
            if (!StringUtils.isBlank(oldRawLocation)) {
                final Matcher oldRawLocationMatcher = OLD_RAW_LOCATION_PATTERN.matcher(oldRawLocation);
                if (oldRawLocationMatcher.find()) {
                    final String newRawLocation = DirectoryConfiguration.getRawDirectory() + oldRawLocationMatcher.group(2);
                    logger.info("Updating node artifact MO raw location from: {}, to: {}", oldRawLocation, newRawLocation);
                    dps.getDataPersistenceService()
                        .getLiveBucket()
                        .findMoByFdn(rawLocationMo.getFdn())
                        .setAttribute(RAW_LOCATION.toString(), newRawLocation);
                }
            }
        });
    }

}
