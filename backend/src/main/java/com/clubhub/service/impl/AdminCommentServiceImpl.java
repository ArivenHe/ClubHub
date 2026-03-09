package com.clubhub.service.impl;

import com.clubhub.dto.AdminCommentResponse;
import com.clubhub.entity.Document;
import com.clubhub.entity.DocumentComment;
import com.clubhub.entity.User;
import com.clubhub.exception.BizException;
import com.clubhub.repository.DocumentCommentRepository;
import com.clubhub.repository.DocumentRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private final DocumentCommentRepository documentCommentRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Override
    public List<AdminCommentResponse> listComments(String keyword, Long documentId) {
        List<DocumentComment> comments = documentCommentRepository.findAllByOrderByCreatedAtDesc();
        if (comments.isEmpty()) {
            return List.of();
        }

        List<Long> documentIds = comments.stream()
            .map(DocumentComment::getDocumentId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, Document> documentMap = documentIds.isEmpty()
            ? Map.of()
            : documentRepository.findByIdIn(documentIds).stream()
                .collect(Collectors.toMap(Document::getId, doc -> doc, (a, b) -> a, HashMap::new));

        List<Long> commentAuthorIds = comments.stream()
            .map(DocumentComment::getAuthorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<Long> documentAuthorIds = documentMap.values().stream()
            .map(Document::getAuthorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<Long> allUserIds = List.copyOf(
            java.util.stream.Stream.concat(commentAuthorIds.stream(), documentAuthorIds.stream())
                .distinct()
                .toList()
        );
        Map<Long, String> userNameMap = allUserIds.isEmpty()
            ? Map.of()
            : userRepository.findByIdIn(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a, HashMap::new));

        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        return comments.stream()
            .filter(comment -> documentId == null || Objects.equals(documentId, comment.getDocumentId()))
            .map(comment -> toResponse(comment, documentMap, userNameMap))
            .filter(response -> {
                if (kw.isEmpty()) {
                    return true;
                }
                return containsIgnoreCase(response.getContent(), kw)
                    || containsIgnoreCase(response.getAuthorName(), kw)
                    || containsIgnoreCase(response.getDocumentTitle(), kw)
                    || containsIgnoreCase(response.getDocumentAuthorName(), kw);
            })
            .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        DocumentComment comment = documentCommentRepository.findById(commentId)
            .orElseThrow(() -> new BizException("评论不存在"));
        documentCommentRepository.delete(comment);
    }

    private AdminCommentResponse toResponse(DocumentComment comment,
                                            Map<Long, Document> documentMap,
                                            Map<Long, String> userNameMap) {
        Long commentAuthorId = comment.getAuthorId();
        String commentAuthorName = commentAuthorId == null ? "未知用户" : userNameMap.getOrDefault(commentAuthorId, "未知用户");

        Long docId = comment.getDocumentId();
        Document document = docId == null ? null : documentMap.get(docId);
        String docTitle = document == null ? "文章已删除" : document.getTitle();

        Long documentAuthorId = document == null ? null : document.getAuthorId();
        String documentAuthorName = documentAuthorId == null
            ? "-"
            : userNameMap.getOrDefault(documentAuthorId, "未知用户");

        return AdminCommentResponse.builder()
            .id(comment.getId())
            .documentId(comment.getDocumentId())
            .documentTitle(docTitle)
            .documentAuthorName(documentAuthorName)
            .authorId(comment.getAuthorId())
            .authorName(commentAuthorName)
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }

    private boolean containsIgnoreCase(String value, String keywordLowerCase) {
        if (value == null || keywordLowerCase == null || keywordLowerCase.isEmpty()) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }
}
