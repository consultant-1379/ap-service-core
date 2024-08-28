package com.ericsson.oss.services.ap.core.rest.model.request.builder;

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiNetworkElement;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiProjectRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectRequestDataBuilder {

    public Map<String,Object> buildProjectRequestData(final EoiProjectRequest projectRequest) {
        final Map<String, Object> elements = new HashMap<>();
        elements.put(ProjectRequestAttributes.PROJECT_NAME.toString(), projectRequest.getName());
        elements.put(ProjectRequestAttributes.CREATOR.toString(), projectRequest.getCreator());
        elements.put(ProjectRequestAttributes.DESCRIPTION.toString(), projectRequest.getDescription());
        elements.put(ProjectRequestAttributes.USE_CASE_TYPE.toString(), projectRequest.getNetworkUsecaseType());
        final List<Map<String, Object>> networkElements = projectRequest.getNetworkElements() == null ? Collections.emptyList()
            : projectRequest.getNetworkElements().stream().map(EoiNetworkElement::toMap).collect(Collectors.toList());

        elements.put(ProjectRequestAttributes.EOI_NETWORK_ELEMENTS.toString(), networkElements);
        elements.put(ProjectRequestAttributes.JSON_PAYLOAD.toString(),projectRequest);
        return elements;
    }
}
