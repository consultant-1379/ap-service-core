#!/bin/bash

###########################################################################
# COPYRIGHT Ericsson 2014
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
###########################################################################

# This script should be executed to add the ${module.name} module
# to the JBoss instance.

#############################################################
#
# Logger Functions
#
#############################################################
info()
{
 logger -s -t TOR_APS_API_MODULE_RPM_POST_INSTALL -p user.notice "INFORMATION ($prg): $@"
}

error()
{
 logger -s -t TOR_APS_API_MODULE_RPM_POST_INSTALL -p user.err "ERROR ($prg): $@"
}

#Building artifact variable from maven properties
artifact=/opt/ericsson/ERICapservicecore_CXP9030533/${tar-gz-artifact-name}
externalModulesDir=/opt/ericsson/jboss/modules
apsApiModuleInstallDir=${externalModulesDir}/com/ericsson/oss/services/autoprovisioning/api/main
oldApsApiModuleInstallDir=${externalModulesDir}/com/ericsson/oss/services/ap/api/main
flowautomationContextDir=/home/shared/autoprovisioning
scriptingSshGroupId=5010

# Create modules dir if it does not exist.
create_modules_dir()
{
     if [ ! -d $externalModulesDir ];
     then
         echo "$externalModulesDir does not exist."
         echo "Creating $externalModulesDir."
         mkdir -p $externalModulesDir
     else
         echo "File $externalModulesDir exists."
     fi
}

#Removes Old files from ap-aps modules directory
remove_old_modules()
{
    if [ -d ${apsApiModuleInstallDir} ]
    then
        info "Cleaning up directory ${apsApiModuleInstallDir}"
        cd ${apsApiModuleInstallDir}; rm -f *
    fi
    if [ -d ${oldApsApiModuleInstallDir} ]
    then
        info "Cleaning up directory ${oldApsApiModuleInstallDir}"
        cd ${oldApsApiModuleInstallDir}; rm -f *
    fi
}

#Adds the module to the JBoss instance(s)
add_module()
{
    info "Unpacking $artifact handler module to external modules directory"
    cd ${externalModulesDir}; tar -xzf $artifact
}

#######################################
# Action :
#   create_flowautomation_context_dir
#   Creates the directory in /home/shared for flowautomation used by AP.
#   Also, sets the permission to 1770 - drwxrwx--T. means,
#   owner (root in this case) and group (scripting_ssh) can read, write to the directory and others can't.
#   the sticky bit is set (T) means, Only the owners or root can delete the files inside the directory.
# Globals :
#   flowautomationContextDir
# Arguments:
#   None
# Returns:
#   1 - Exits with 1 if the directory creation fails.
#######################################
create_flowautomation_context_dir() {
    if [ ! -d "$flowautomationContextDir" ];
    then
        echo "Creating $flowautomationContextDir."
        mkdir -p -m 1770 "$flowautomationContextDir"
        if [[ $? -ne 0 ]];
        then
            error "Failed to create $flowautomationContextDir"
            exit 1
        fi
    else
        echo "Directory $flowautomationContextDir exists."
    fi

    chown "root":$scriptingSshGroupId "$flowautomationContextDir"
}

#Execute
create_modules_dir
remove_old_modules
add_module
create_flowautomation_context_dir

#Exit code set to 0 to allow cmw campaign to succeed
exit 0
