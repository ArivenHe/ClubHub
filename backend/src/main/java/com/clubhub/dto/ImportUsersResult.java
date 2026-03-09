package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportUsersResult {

    private int successCount;
    private int skipCount;
    private List<String> skipReasons;
}
