package com.ericsson.oss.services.ap.core.rest.model.request;

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;

import java.util.HashMap;
import java.util.Map;

public class EoiNetworkElement {

    private static final String ENABLED = "enabled";

    private static final String DISABLED = "disabled";
    private String nodeName = "";

    private String ipAddress = "";

    private String neType = "";

    private String userName = "";

    private String password = "";

    private String ossPrefix = "";

    private String modelVersion = "";

    private String timezone = "";

    private String subjectAltName = "";

    private String cnfType = "";

    public String getNeType() {
        return neType;
    }

    public void setNeType(String neType) {
        this.neType = neType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOssPrefix() {
        return ossPrefix;
    }

    public void setOssPrefix(String ossPrefix) {
        this.ossPrefix = ossPrefix;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getSubjectAltName() {
        return subjectAltName;
    }

    public void setSubjectAltName(String subjectAltName) {
        this.subjectAltName = subjectAltName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCnfType() {
        return cnfType;
    }

    public void setCnfType(final String cnfType) {
        this.cnfType = cnfType;
    }

    public class Supervision {

        @Override
        public String toString() {
            return "Supervision [pm=" + pm + ", fm=" + fm + ", cm=" + cm + "]";
        }

        private boolean pm;
        private boolean fm;
        private boolean cm;

        public boolean getPm() {
            return pm;

        }

        public void setPm(boolean pm) {
            this.pm = pm;
        }

        public boolean getFm() {
            return fm;

        }

        public void setFm(boolean fm) {
            this.fm = fm;
        }

        public boolean getCm() {
            return cm;

        }

        public void setCm(boolean cm) {
            this.cm = cm;
        }
    }

    private Supervision supervision;

    @Override
    public String toString() {
        return "EoiNetworkElement{" +
            "nodeName='" + nodeName + '\'' +
            ", ipAddress='" + ipAddress + '\'' +
            ", neType='" + neType + '\'' +
            ", userName='" + userName + '\'' +
            ", password='" + password + '\'' +
            ", ossPrefix='" + ossPrefix + '\'' +
            ", modelVersion='" + modelVersion + '\'' +
            ", timezone='" + timezone + '\'' +
            ", subjectAltName='" + subjectAltName + '\'' +
            ", supervision=" + supervision +
            ", cnfType='" + cnfType + '\'' +
            '}';
    }

    public Supervision getSupervision() {
        return supervision;
    }

    public void setSupervision(final Supervision supervision) {
        this.supervision = supervision;
    }

    public static Map<String, Object> toMap(final EoiNetworkElement eoiNetworkElements) {
        final Map<String, Object> fileMap = new HashMap<>();
        final Map<String, Object> supervisionAttributes = new HashMap<>();
        if (eoiNetworkElements != null) {
            fileMap.put(ProjectRequestAttributes.NODE_NAME.toString(), eoiNetworkElements.getNodeName());
            fileMap.put(ProjectRequestAttributes.NODE_TYPE.toString(), eoiNetworkElements.getNeType());
            fileMap.put(ProjectRequestAttributes.CNF_TYPE.toString(), eoiNetworkElements.getCnfType());
            fileMap.put(ProjectRequestAttributes.IPADDRESS.toString(), eoiNetworkElements.getIpAddress());
            fileMap.put(ProjectRequestAttributes.TIME_ZONE.toString(), eoiNetworkElements.getTimezone());
            fileMap.put(ProjectRequestAttributes.OSS_PREFIX.toString(), eoiNetworkElements.getOssPrefix());
            fileMap.put(ProjectRequestAttributes.NODE_IDENTIFIER.toString(), eoiNetworkElements.getModelVersion());
            fileMap.put(ProjectRequestAttributes.USER_NAME.toString(), eoiNetworkElements.getUserName());
            fileMap.put(ProjectRequestAttributes.PASSWORD.toString(), eoiNetworkElements.getPassword());
            fileMap.put(ProjectRequestAttributes.SUBJECT_ALT_NAME.toString(), eoiNetworkElements.getSubjectAltName());
            final Supervision superVision = eoiNetworkElements.getSupervision();
            supervisionAttributes.put("cm", superVision.getCm() ? ENABLED : DISABLED);
            supervisionAttributes.put("pm", superVision.getPm() ? ENABLED : DISABLED);
            supervisionAttributes.put("fm", superVision.getFm() ? ENABLED : DISABLED);
            fileMap.put(ProjectRequestAttributes.SUPERVISION_ATTRIBUTES.toString(), supervisionAttributes);
        }
        return fileMap;
    }

}
