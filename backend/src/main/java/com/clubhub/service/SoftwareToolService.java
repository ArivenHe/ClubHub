package com.clubhub.service;

import com.clubhub.dto.CreateSoftwareToolRequest;
import com.clubhub.dto.SoftwareToolResponse;

import java.util.List;

public interface SoftwareToolService {

    SoftwareToolResponse createTool(CreateSoftwareToolRequest request);

    List<SoftwareToolResponse> listTools();
}
