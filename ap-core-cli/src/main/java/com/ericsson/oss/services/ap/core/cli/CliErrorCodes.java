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

/**
 * Class containing the CLI error codes that will be returned to user when an error occurs.
 */
public final class CliErrorCodes {

    public static final int ENTITY_NOT_FOUND_ERROR_CODE = 16002;
    public static final int CRUD_ERROR_CODE = 16003;
    public static final int SERVICE_ERROR_CODE = 16004;
    public static final int NO_ATTACHED_FILE_ERROR_CODE = 16005;
    public static final int UNSUPPORTED_NODE_TYPE_ERROR_CODE = 16006;
    public static final int INVALID_STATE_ERROR_CODE = 16007;
    public static final int VALIDATION_ERROR_CODE = 16008;
    public static final int INVALID_COMMAND_SYNTAX_ERROR_CODE = 16009;
    public static final int NO_AUTHORIZATION_ERROR_CODE = 16010;
    public static final int UNSUPPORTED_COMMAND_ERROR_CODE = 16011;
    public static final int ILLEGAL_OPERATION_ERROR_CODE = 16012;
    public static final int HARDWARE_REPLACE_PROJECT_DELETE_ERROR_CODE = 16013;

    private CliErrorCodes() {

    }
}
