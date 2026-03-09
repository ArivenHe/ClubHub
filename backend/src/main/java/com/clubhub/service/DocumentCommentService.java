package com.clubhub.service;

import com.clubhub.dto.CreateDocumentCommentRequest;
import com.clubhub.dto.DocumentCommentResponse;

import java.util.List;

public interface DocumentCommentService {

    List<DocumentCommentResponse> listComments(Long documentId);

    DocumentCommentResponse createComment(Long documentId, CreateDocumentCommentRequest request);

    void deleteComment(Long commentId);
}
