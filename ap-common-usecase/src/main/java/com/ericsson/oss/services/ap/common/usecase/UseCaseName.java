/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.usecase;

/**
 * Names of the AutoProvisioning usecases. Also provides method to retrieve their related CLI command.
 */
public enum UseCaseName {

    BATCH_BIND(UseCase.BIND),
    BIND(UseCase.BIND),
    CANCEL(UseCase.CANCEL),
    CREATE_PROFILE(UseCase.CREATE),
    CREATE_PROJECT(UseCase.CREATE),
    DELETE(UseCase.DELETE),
    DELETE_NODE(UseCase.DELETE),
    DELETE_PROFILE(UseCase.DELETE),
    DELETE_PROJECT(UseCase.DELETE),
    DOWNLOAD(UseCase.DOWNLOAD),
    DOWNLOAD_ARTIFACT(UseCase.DOWNLOAD),
    DOWNLOAD_SCHEMA_SAMPLE(UseCase.DOWNLOAD),
    DOWNLOAD_CIQ(UseCase.DOWNLOAD),
    DUMP_SNAPSHOT(UseCase.DUMP),
    EXPORT_CIQ(UseCase.EXPORT),
    GET_SNAPSHOT(UseCase.GET),
    IMPORT(UseCase.ORDER),
    INTEGRATE(UseCase.ORDER),
    MODIFY_PROFILE(UseCase.MODIFY),
    ORDER(UseCase.ORDER),
    ORDER_NODE(UseCase.ORDER),
    ORDER_PROJECT(UseCase.ORDER),
    EOI_ORDER_NODE(UseCase.EOI_ORDER),
    EOI_ORDER_PROJECT(UseCase.EOI_ORDER),
    RESTORE(UseCase.INVALID),
    RESUME(UseCase.RESUME),
    SKIP(UseCase.SKIP),
    STATUS(UseCase.STATUS),
    STATUS_ALL_PROJECTS(UseCase.STATUS),
    STATUS_DEPLOYMENT(UseCase.STATUS),
    STATUS_NODE(UseCase.STATUS),
    STATUS_PROJECT(UseCase.STATUS),
    UNSUPPORTED(UseCase.INVALID),
    UPLOAD_ARTIFACT(UseCase.UPLOAD),
    VIEW(UseCase.VIEW),
    VIEW_ALL_PROFILES(UseCase.VIEW),
    VIEW_ALL_PROJECTS(UseCase.VIEW),
    VIEW_ALL_NODES(UseCase.VIEW),
    VIEW_NODE(UseCase.VIEW),
    VIEW_NODE_TYPES(UseCase.VIEW),
    VIEW_PROFILES(UseCase.VIEW),
    VIEW_PROJECT(UseCase.VIEW),
    VIEW_TEMPLATES(UseCase.VIEW);

    private String cliCommand;

    private UseCaseName(final String cliCommand) {
        this.cliCommand = cliCommand;
    }

    /**
     * The usecase's corresponding CLI command.
     *
     * @return String
     *            the command string
     */
    public String cliCommand() {
        return cliCommand;
    }

    public static UseCaseName getUseCaseName(final String name) {
        for (final UseCaseName ucName : UseCaseName.class.getEnumConstants()) {
            if (ucName.name().equals(name)) {
                return ucName;
            }
        }
        throw new IllegalArgumentException("Unknown usecase " + name);
    }

    private static class UseCase {

        public static final String BIND = "bind";
        public static final String CANCEL = "cancel";
        public static final String CREATE = "create";
        public static final String DELETE = "delete";
        public static final String DOWNLOAD = "download";
        public static final String DUMP = "dump";
        public static final String EXPORT = "export";
        public static final String GET = "get";
        public static final String INVALID = "invalid";
        public static final String MODIFY = "modify";
        public static final String ORDER = "order";
        public static final String EOI_ORDER = "eoiOrder";
        public static final String RESUME = "resume";
        public static final String SKIP = "skip";
        public static final String STATUS = "status";
        public static final String UPLOAD = "upload";
        public static final String VIEW = "view";
    }
}
