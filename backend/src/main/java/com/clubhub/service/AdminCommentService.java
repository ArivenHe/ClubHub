package com.clubhub.service;

import com.clubhub.dto.AdminCommentResponse;

import java.util.List;

public interface AdminCommentService {

    List<AdminCommentResponse> listComments(String keyword, Long documentId);

    void deleteComment(Long commentId);
}
