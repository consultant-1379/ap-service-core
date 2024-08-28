#!/bin/bash
###########################################################################
# COPYRIGHT Ericsson 2016
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
###########################################################################

_MKDIR=/bin/mkdir
_CHOWN=/bin/chown
_MV=/bin/mv
_LS=/bin/ls
_GREP=/bin/grep
_RM=/bin/rm
_CP=/bin/cp
_LN=/bin/ln

JBOSS_USR=${jboss-username}
JBOSS_GRP=${jboss-groupname}

APP_NAME="ap"
VM_DIRECTORY="/opt/ap-service-core/data/json"
MOUNT_DIRECTORY="/ericsson/config_mgt"
JSON_DIRECTORY="/ericsson/config_mgt/ebnfToJson"
JSON_FILES="/ericsson/config_mgt/ebnfToJson/"${APP_NAME}"/"*.json
APP_DST_DIR=${JSON_DIRECTORY}"/"${APP_NAME}

BASENAME=/bin/basename
SCRIPT_NAME=`${BASENAME} ${0}`


#######################################
# This function will print an error message to /var/log/messages
# Arguments:
#       $1 - Message
# Return: 0
#######################################
error()
{
    logger -s -t JBOSS -p user.err "ERROR ( ${SCRIPT_NAME} ): $1"
}


#######################################
# This function will print an info message to /var/log/messages
# Arguments:
#       $1 - Message
# Return: 0
#######################################
info()
{
    logger -s -t JBOSS -p user.notice "INFO ( ${SCRIPT_NAME} ): $1"
}


#######################################
create_dir() {
    $_MKDIR -m 770 $1
    if [ $? -ne 0 ];then
        error "Failed to create $1"
        exit 1
    fi
        info "Created directory $1"
}


#######################################
create_json_file_dir() {
    if ! [ -d "$JSON_DIRECTORY" ]; then
        create_dir $JSON_DIRECTORY
    fi

    if ! [ -d "$APP_DST_DIR" ]; then
        create_dir $APP_DST_DIR
    else
        info "$APP_DST_DIR already exists."
        #remove old json files on $JSON_DIRECTORY
        remove_old_generation_jsons

        #remove old json files on $JSON_DIRECTORY
        remove_symlink

        # Removal of old JSON files from application directory
        $_RM -f ${APP_DST_DIR}/*.json
        info "Removed ... $APP_DST_DIR/*.json"
     fi
}

#######################################
create_symbolic_links(){
    $_LN -s ${APP_DST_DIR}/*.json ${JSON_DIRECTORY}
        if [ $? -ne 0 ];then
            error "Failed to create symbolic links $JSON_DIRECTORY"
            exit 1
        else
            info "Symbolic links created"
        fi
}

#######################################
remove_old_generation_jsons(){
    oldJsonFiles=$($_LS ${VM_DIRECTORY} |$_GREP '\.json$')
    for oldJsonFile in $oldJsonFiles; do
        $_RM -f ${JSON_DIRECTORY}/$(basename ${oldJsonFile})
        if [ $? -ne 0 ];then
            error "Failed to remove old ${oldJsonFile} file from ${JSON_DIRECTORY}"
            exit 1
        fi
    done
    info "Removed old json files from ${JSON_DIRECTORY}"
}

#######################################
remove_symlink(){
    oldSymLinks=$($_LS ${APP_DST_DIR} |$_GREP '\.json$')
    for oldSymLink in $oldSymLinks; do
        $_RM -f ${JSON_DIRECTORY}/$(basename ${oldSymLink})
        if [ $? -ne 0 ];then
            error "Failed to remove old ${oldSymLink} file from ${JSON_DIRECTORY}"
            exit 1
        fi
    done
    info "Removed old symbolic links file from ${JSON_DIRECTORY}"
}

#######################################
copy_json_fromVM_toNAS(){
    numfiles=$($_LS | $_GREP -c '\.json$')
    if [ "$numfiles" -eq 0 ];then
        error ${VM_DIRECTORY} " doesn't contain any JSON files"
        exit 1
    else
        info "Found $numfiles JSON file(s) in $VM_DIRECTORY"
        $_CP -f ${VM_DIRECTORY}/*.json ${APP_DST_DIR}
    fi
    if [ $? -ne 0 ];then
        error "Failed to copy files to $APP_DST_DIR"
        exit 1
    else
        cd ${APP_DST_DIR}
        info "Copied $numfiles JSON file(s) into " ${APP_DST_DIR}
        # creation of links
        create_symbolic_links
    fi
}

#######################################
move_files() {
    if  [ -d ${VM_DIRECTORY} ]; then
        cd ${VM_DIRECTORY}
        #copy source vm directory json files to NAS shared ebnftojson directory
        copy_json_fromVM_toNAS
    fi
}


#######################################
sanity_check(){
    listLinks=$(find ${JSON_DIRECTORY} -type l -ls)
    info "$listLinks"

    #List the symbolic links broken if present
    numBrocken=$(find -L ${JSON_DIRECTORY} -type l | wc -l)
    if [ "$numBrocken" != "0" ];then
        info "########################################################"
        info "List of brocken link(s):"
        listBroken=$(find  -L ${JSON_DIRECTORY} -type l)
        print_file_list $listBroken
    fi
}

#######################################
print_file_list(){
    for fileName in $@ ; do
        info "$fileName"
    done
}

#######################################
while true;
do
    info "############ Grammar JSON migration starts #############"
    info ""
    info "################ INIT CONSISTENCY CHECK ################"
    info "Symbolic link(s) present before JSON migration in $JSON_DIRECTORY:"
    sanity_check
    info "################ END CONSISTENCY CHECK #################"
    if [ -d "$MOUNT_DIRECTORY" ]; then
        info "Creating json directory now ..."
        create_json_file_dir
        info "Moving the json files now..."
        move_files
        $_CHOWN -R ${JBOSS_USR}:${JBOSS_GRP} ${JSON_DIRECTORY}
        info "Changed ownership of json files ..."
    fi
    info "################ INIT CONSISTENCY CHECK ################"
    info "Symbolic link(s) present after JSON migration in $JSON_DIRECTORY:"
    sanity_check
    info "################ END CONSISTENCY CHECK #################"
    info ""
    info "############ Grammar JSON migration ends #############"
    exit 0
done
