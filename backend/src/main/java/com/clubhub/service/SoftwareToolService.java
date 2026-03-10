package com.clubhub.service;

import com.clubhub.dto.CreateSoftwareToolRequest;
import com.clubhub.dto.SoftwareToolResponse;
import com.clubhub.enums.SoftwareToolStatus;

import java.util.List;

public interface SoftwareToolService {

    SoftwareToolResponse createTool(CreateSoftwareToolRequest request);

    SoftwareToolResponse submitApplication(CreateSoftwareToolRequest request);

    List<SoftwareToolResponse> listTools();

    List<SoftwareToolResponse> adminListTools();

    SoftwareToolResponse reviewTool(Long toolId, SoftwareToolStatus status, String reviewRemark);

    void deleteTool(Long toolId);
}
