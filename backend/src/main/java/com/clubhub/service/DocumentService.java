package com.clubhub.service;

import com.clubhub.dto.CreateDocumentRequest;
import com.clubhub.dto.DocumentResponse;
import com.clubhub.enums.DocumentStatus;

import java.util.List;

public interface DocumentService {

    DocumentResponse createDocument(CreateDocumentRequest request);

    DocumentResponse updateMyDocument(Long documentId, CreateDocumentRequest request);

    List<DocumentResponse> listMyDocuments();

    List<DocumentResponse> listAllDocuments();

    List<DocumentResponse> listRecommendedDocuments(int limit);

    DocumentResponse getDocumentDetail(Long documentId);

    DocumentResponse likeDocument(Long documentId);

    DocumentResponse unlikeDocument(Long documentId);

    void deleteMyDocument(Long documentId);

    List<DocumentResponse> adminListDocuments(String keyword, DocumentStatus status, Boolean recommended);

    DocumentResponse adminUpdateStatus(Long documentId, DocumentStatus status);

    DocumentResponse adminUpdateRecommended(Long documentId, boolean recommended);

    void adminDeleteDocument(Long documentId);
}
