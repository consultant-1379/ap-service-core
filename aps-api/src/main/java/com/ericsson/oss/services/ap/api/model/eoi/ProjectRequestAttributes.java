package com.ericsson.oss.services.ap.api.model.eoi;

public enum ProjectRequestAttributes {

    PROJECT_NAME("projectname"),
    CREATOR("creator"),
    DESCRIPTION("description"),
    USE_CASE_TYPE("useCaseType"),
    NODE_NAME("name"),
    NODE_TYPE("nodeType"),
    IPADDRESS("ipAddress"),
    CNF_TYPE("cnfType"),
    MODEL_VERSION("modelVersion"),
    TIME_ZONE("timeZone"),
    OSS_PREFIX("ossPrefix"),
    NODE_IDENTIFIER("nodeIdentifier"),
    JSON_PAYLOAD("jsonPayload"),
    USER_NAME("secureUserName"),
    PASSWORD("securePassword"),
    SUBJECT_ALT_NAME("subjectAltName"),
    WORKFLOW_INSTANCE_ID_LIST("workflowInstanceIdList"),
    EOI_NETWORK_ELEMENTS("networkelements"),
    SUPERVISION_ATTRIBUTES("SupervisionOptions");


    private String attributeName;

    ProjectRequestAttributes(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
