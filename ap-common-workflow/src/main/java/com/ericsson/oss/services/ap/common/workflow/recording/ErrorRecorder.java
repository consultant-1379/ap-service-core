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
package com.ericsson.oss.services.ap.common.workflow.recording;

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;

import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Records error during Order/Delete/Integrate.
 */
public class ErrorRecorder {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private SystemRecorder systemRecorder;

    public void validationFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Validation of files failed for node: projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.VALIDATE_CONFIGURATIONS.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void addNodeFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Add node failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void updateNodeFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Update node failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void createNodeCredentialsFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Create Node User Credentials failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void removeNodeFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Remove node failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.DELETE_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void generatedSecurityFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Generate Security failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void importConfigurationsFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Import Configurations failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + e.getMessage());
    }

    public void cancelSecurityFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Cancel Security failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.DELETE_NODE.toString(), ErrorSeverity.WARNING, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void bindNodeFailed(final String apNodeFdn, final String hardwareSerialNumber, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Bind failed -> projectname=%s, nodename=%s, hwId=%s", projectName, nodeName, hardwareSerialNumber);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.BIND.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void createdGeneratedArtifactFailed(final String apNodeFdn, final String artifactType, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Error generating artifact -> projectname=%s, nodeName=%s, type=%s", projectName, nodeName,
                artifactType);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void uploadArtifactFailed(final String apNodeFdn, final String fileName, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Upload Artifact failed -> projectname=%s, nodename=%s, fileName=%s", projectName, nodeName, fileName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.UPLOAD_ARTIFACT.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void startVnfFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Start VNF failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void deleteVnfFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Delete VNF failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.DELETE_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void rollbackVnfFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Rollback VNF lifecycle failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void deleteGeneratedArtifactFailed(final String apNodeFdn, final String artifactType, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Delete generated artifact failed -> projectname=%s, nodeName=%s, type=%s", projectName, nodeName,
                artifactType);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.DELETE_NODE.toString(), ErrorSeverity.WARNING, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void syncNodeFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Sync node failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void updateSupervisionStatusFailed(final String apNodeFdn, final String supervisionMoType, final Exception e, final String enableDisableSupervision, final String state) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("%s %s supervision failed -> projectname=%s, nodename=%s", enableDisableSupervision, supervisionMoType, projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(state, ErrorSeverity.ERROR, nodeName, networkElementFdn,
            errorMsg + "\n" + getRootCause(e));
    }

    public void activateOptionalFeaturesFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Activate Optional Features failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void unlockCellsFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Unlock Cells failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void createCvFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Create CV failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void uploadCvFailed(final String apNodeFdn) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Upload CV failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn, errorMsg);
    }

    public void uploadCvFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Upload CV failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void initiateGpsPositionCheckFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Failed to initiate GPS position check -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void createBackupFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Create backup failed -> projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + e.getMessage());
    }

    public void removeBackupFailed(final String apNodeFdn, final String backupName, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Remove backup failed -> projectname=%s, nodename=%s, backupname=%s", projectName, nodeName,
                backupName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.WARNING, nodeName, networkElementFdn,
                errorMsg + "\n" + e.getMessage());
    }

    public void restoreBackupFailed(final String apNodeFdn, final String backupName, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String networkElementFdn = NETWORK_ELEMENT.toString() + "=" + nodeName;
        final String errorMsg = String.format("Restore backup failed -> projectname=%s, nodename=%s, backupname=%s", projectName, nodeName,
                backupName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, networkElementFdn,
                errorMsg + "\n" + e.getMessage());
    }

    public void uploadBackupFailed(final String apNodeFdn, final String backupName, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Upload backup failed -> projectname=%s, nodename=%s, backupname=%s", projectName, nodeName,
                backupName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.WARNING, nodeName, apNodeFdn, errorMsg + "\n" + e.getMessage());
    }

    public void updateRbsConfigLevelFailed(final String apNodeFdn, final String rbsConfigLevelValue, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Error updating rbsConfigLevel for project %s node %s , rbsConfigLevel=%s", projectName, nodeName,
                rbsConfigLevelValue);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg + "\n" + getRootCause(e));
    }

    public void setSWMFailed(final String apNodeFdn) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Error configuring Software Management attribute for project %s node %s", projectName, nodeName);
        logger.error(errorMsg);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn, errorMsg);
    }

    public void importLicenseKeyFileFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Import License key file to SHM failed for node: projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.IMPORT_LICENSEKEYFILE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void installLicenseKeyFileFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Install License key file failed for node: projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.INSTALL_LICENSE_KEY_FILE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));
    }

    public void deleteLicenseKeyFileFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Delete License key file failed for node: projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.DELETE_LICENSE_KEY_FILE.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
                errorMsg + "\n" + getRootCause(e));

    }

    public void assignTargetGroupFailed(final String apNodeFdn, final Exception e) {
        final String nodeName = getNodeName(apNodeFdn);
        final String projectName = getProjectName(apNodeFdn);
        final String errorMsg = String.format("Assign TargetGroup failed for node: projectname=%s, nodename=%s", projectName, nodeName);
        logger.error(errorMsg, e);
        systemRecorder.recordError(CommandLogName.ASSIGN_TARGET_GROUPS.toString(), ErrorSeverity.ERROR, nodeName, apNodeFdn,
            errorMsg + "\n" + getRootCause(e));
    }

    private static String getNodeName(final String apNodeFdn) {
        return FDN.get(apNodeFdn).getRdnValue();
    }

    private static String getProjectName(final String apNodeFdn) {
        final String projectFdn = FDN.get(apNodeFdn).getParent();
        return FDN.get(projectFdn).getRdnValue();
    }

    private static String getRootCause(final Exception e) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);
        return rootCause == null ? e.getMessage() : rootCause.getMessage();
    }
}
