package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.CreateDocumentCommentRequest;
import com.clubhub.dto.DocumentCommentResponse;
import com.clubhub.entity.Document;
import com.clubhub.entity.DocumentComment;
import com.clubhub.entity.User;
import com.clubhub.enums.DocumentStatus;
import com.clubhub.exception.BizException;
import com.clubhub.repository.DocumentCommentRepository;
import com.clubhub.repository.DocumentRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.DocumentCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentCommentServiceImpl implements DocumentCommentService {

    private final DocumentRepository documentRepository;
    private final DocumentCommentRepository documentCommentRepository;
    private final UserRepository userRepository;

    @Override
    public List<DocumentCommentResponse> listComments(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = findDocument(documentId);
        assertReadable(document, userId);

        List<DocumentComment> comments = documentCommentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId);
        if (comments.isEmpty()) {
            return List.of();
        }

        List<Long> authorIds = comments.stream()
            .map(DocumentComment::getAuthorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> authorMap = authorIds.isEmpty() ? Map.of() : userRepository.findByIdIn(authorIds).stream()
            .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));

        boolean canManage = StpUtil.hasPermission("doc:manage");
        boolean isArticleOwner = Objects.equals(userId, document.getAuthorId());

        return comments.stream()
            .map(comment -> toResponse(
                comment,
                authorMap,
                userId,
                canManage,
                isArticleOwner
            ))
            .toList();
    }

    @Override
    @Transactional
    public DocumentCommentResponse createComment(Long documentId, CreateDocumentCommentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = findDocument(documentId);
        assertReadable(document, userId);

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            throw new BizException("评论内容不能为空");
        }

        DocumentComment comment = new DocumentComment();
        comment.setDocumentId(documentId);
        comment.setAuthorId(userId);
        comment.setContent(content);
        comment = documentCommentRepository.save(comment);

        String authorName = userRepository.findById(userId).map(User::getRealName).orElse("未知用户");
        boolean canManage = StpUtil.hasPermission("doc:manage");
        boolean isArticleOwner = Objects.equals(userId, document.getAuthorId());
        return DocumentCommentResponse.builder()
            .id(comment.getId())
            .documentId(comment.getDocumentId())
            .authorId(comment.getAuthorId())
            .authorName(authorName)
            .content(comment.getContent())
            .canDelete(canManage || isArticleOwner || Objects.equals(userId, comment.getAuthorId()))
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        DocumentComment comment = documentCommentRepository.findById(commentId)
            .orElseThrow(() -> new BizException("评论不存在"));
        Document document = findDocument(comment.getDocumentId());

        boolean canManage = StpUtil.hasPermission("doc:manage");
        boolean isCommentOwner = Objects.equals(userId, comment.getAuthorId());
        boolean isArticleOwner = Objects.equals(userId, document.getAuthorId());
        if (!canManage && !isCommentOwner && !isArticleOwner) {
            throw new BizException("无权限删除该评论");
        }

        documentCommentRepository.delete(comment);
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));
    }

    private void assertReadable(Document document, Long userId) {
        boolean canManage = StpUtil.hasPermission("doc:manage");
        boolean isOwner = Objects.equals(userId, document.getAuthorId());
        boolean isPublished = DocumentStatus.PUBLISHED.equals(document.getStatus());
        if (!isPublished && !isOwner && !canManage) {
            throw new BizException("无权限查看该文章");
        }
    }

    private DocumentCommentResponse toResponse(DocumentComment comment,
                                               Map<Long, String> authorMap,
                                               Long userId,
                                               boolean canManage,
                                               boolean isArticleOwner) {
        Long authorId = comment.getAuthorId();
        String authorName = authorId == null ? "未知用户" : authorMap.getOrDefault(authorId, "未知用户");
        boolean canDelete = canManage || isArticleOwner || Objects.equals(userId, authorId);
        return DocumentCommentResponse.builder()
            .id(comment.getId())
            .documentId(comment.getDocumentId())
            .authorId(comment.getAuthorId())
            .authorName(authorName)
            .content(comment.getContent())
            .canDelete(canDelete)
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }
}
