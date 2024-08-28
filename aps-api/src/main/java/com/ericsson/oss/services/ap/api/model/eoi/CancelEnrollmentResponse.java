package com.ericsson.oss.services.ap.api.model.eoi;

public class CancelEnrollmentResponse {

        private String resource;
        private String resourceId;
        private String subResource;
        private String subResourceId;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSubResource() {
        return subResource;
    }

    public void setSubResource(String subResource) {
        this.subResource = subResource;
    }

    public String getSubResourceId() {
        return subResourceId;
    }

    public void setSubResourceId(String subResourceId) {
        this.subResourceId = subResourceId;
    }

    @Override
    public String toString() {
        return "CancelEnrollmentResponse{" +
            "resource='" + resource + '\'' +
            ", resourceId='" + resourceId + '\'' +
            ", subResource='" + subResource + '\'' +
            ", subResourceId='" + subResourceId + '\'' +
            '}';
    }
}
