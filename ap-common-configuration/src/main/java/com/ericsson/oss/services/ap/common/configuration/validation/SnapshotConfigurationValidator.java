/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.configuration.validation;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.model.ConfigSnapshotStatus;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;

/**
 * Check Snapshot Configuration Status.
 */
public class SnapshotConfigurationValidator {

    @Inject
    private DpsOperations dps;

    @Inject
    private Logger logger;

    public String getNodeConfigSnapshotStatus(final String profileFdn) {
        final ManagedObject profileMo = getProfileMo(profileFdn);
        return profileMo.getAttribute(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString()).toString();
    }

    public boolean readyTriggerSnapshotContent(final String profileFdn) {
        final String status = getNodeConfigSnapshotStatus(profileFdn);
        if (ConfigSnapshotStatus.NOT_STARTED.toString().equals(status) || (ConfigSnapshotStatus.FAILED.toString().equals(status)) ||
                (checkTimeStampTimeout(profileFdn))) {
            return true;
        } else {
            throw new ApApplicationException(String.format("Snapshot dumping is ongoing for %s, another triggering is not allowed", profileFdn));
        }
    }

    public boolean readyGetSnapshotContent(final String profileFdn) {
        final String status = getNodeConfigSnapshotStatus(profileFdn);

        if (!ConfigSnapshotStatus.COMPLETED.toString().equals(status) && !ConfigSnapshotStatus.IN_PROGRESS.toString().equals(status)) {
            throw new ApApplicationException("Fail to get snapshot under unexpected snapshot status " + status);
        }

        return ConfigSnapshotStatus.COMPLETED.toString().equals(status);
    }

    private ManagedObject getProfileMo(final String profileFdn) {
        final ManagedObject profileMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(profileFdn);
        if (profileMo == null) {
            throw new ProfileNotFoundException(profileFdn);
        }
        return profileMo;
    }

    private boolean checkTimeStampTimeout(final String profileFdn) {
        try {
            final long minIntervalMilliseconds = 60000L;
            final Long iniTime = getProfileMo(profileFdn).getAttribute(ProfileAttribute.DUMP_TIMESTAMP.toString());
            if (iniTime == null) {
                logger.warn("No dumpTimeStamp attribute for node Fdn {}", profileFdn);
                return false;
            }
            return ((System.currentTimeMillis() - iniTime) > minIntervalMilliseconds);
        } catch (Exception exception) {
            logger.warn("Failure when check timestamp ", exception);
            throw exception;
        }
    }
}
