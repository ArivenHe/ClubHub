package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.CreateDocumentRequest;
import com.clubhub.dto.DocumentResponse;
import com.clubhub.entity.Document;
import com.clubhub.entity.DocumentLike;
import com.clubhub.entity.Tag;
import com.clubhub.entity.User;
import com.clubhub.enums.DocumentStatus;
import com.clubhub.exception.BizException;
import com.clubhub.repository.DocumentCommentRepository;
import com.clubhub.repository.DocumentLikeRepository;
import com.clubhub.repository.DocumentRepository;
import com.clubhub.repository.TagRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.DocumentService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCommentRepository documentCommentRepository;
    private final DocumentLikeRepository documentLikeRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setSummary(request.getSummary());
        document.setContent(request.getContent());
        document.setSemesterTag(request.getSemesterTag());
        document.setTagId(resolveTagId(request.getTagId()));
        document.setStatus(request.getStatus());
        document.setAuthorId(userId);

        Document saved = documentRepository.save(document);
        User user = userRepository.findById(userId).orElse(null);

        return toResponse(saved, user == null ? "未知用户" : user.getRealName(), findTagName(saved.getTagId()), 0L, false);
    }

    @Override
    @Transactional
    public DocumentResponse updateMyDocument(Long documentId, CreateDocumentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));

        boolean canManage = StpUtil.hasPermission("doc:manage");
        if (!canManage && !Objects.equals(userId, document.getAuthorId())) {
            throw new BizException("只能编辑自己的文章");
        }

        document.setTitle(request.getTitle());
        document.setSummary(request.getSummary());
        document.setContent(request.getContent());
        document.setSemesterTag(request.getSemesterTag());
        document.setTagId(resolveTagId(request.getTagId()));
        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }

        Document saved = documentRepository.save(document);
        String authorName = findAuthorName(saved.getAuthorId());
        String tagName = findTagName(saved.getTagId());
        long likeCount = documentLikeRepository.countByDocumentId(saved.getId());
        boolean liked = documentLikeRepository.existsByDocumentIdAndUserId(saved.getId(), userId);
        return toResponse(saved, authorName, tagName, likeCount, liked);
    }

    @Override
    public List<DocumentResponse> listMyDocuments() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Document> documents = documentRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        return toResponses(documents, userId);
    }

    @Override
    public List<DocumentResponse> listAllDocuments() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Document> documents = documentRepository.findByStatusOrderByCreatedAtDesc(DocumentStatus.PUBLISHED);
        return toResponses(documents, userId);
    }

    @Override
    public List<DocumentResponse> listRecommendedDocuments(int limit) {
        Long userId = StpUtil.getLoginIdAsLong();
        int finalLimit = limit <= 0 ? 6 : Math.min(limit, 20);
        List<Document> documents = documentRepository.findByStatusAndRecommendedTrueOrderByUpdatedAtDesc(
            DocumentStatus.PUBLISHED, PageRequest.of(0, finalLimit)
        );
        return toResponses(documents, userId);
    }

    @Override
    @Transactional
    public DocumentResponse getDocumentDetail(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));

        assertReadable(document, userId);
        documentRepository.incrementViewCountById(document.getId());
        long currentViewCount = document.getViewCount() == null ? 0L : document.getViewCount();
        document.setViewCount(currentViewCount + 1);

        String authorName = findAuthorName(document.getAuthorId());
        String tagName = findTagName(document.getTagId());
        long likeCount = documentLikeRepository.countByDocumentId(document.getId());
        boolean liked = documentLikeRepository.existsByDocumentIdAndUserId(document.getId(), userId);
        return toResponse(document, authorName, tagName, likeCount, liked);
    }

    @Override
    @Transactional
    public DocumentResponse likeDocument(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));
        assertReadable(document, userId);

        if (!documentLikeRepository.existsByDocumentIdAndUserId(documentId, userId)) {
            DocumentLike like = new DocumentLike();
            like.setDocumentId(documentId);
            like.setUserId(userId);
            documentLikeRepository.save(like);
        }

        String authorName = findAuthorName(document.getAuthorId());
        String tagName = findTagName(document.getTagId());
        long likeCount = documentLikeRepository.countByDocumentId(document.getId());
        return toResponse(document, authorName, tagName, likeCount, true);
    }

    @Override
    @Transactional
    public DocumentResponse unlikeDocument(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));
        assertReadable(document, userId);

        documentLikeRepository.deleteByDocumentIdAndUserId(documentId, userId);

        String authorName = findAuthorName(document.getAuthorId());
        String tagName = findTagName(document.getTagId());
        long likeCount = documentLikeRepository.countByDocumentId(document.getId());
        return toResponse(document, authorName, tagName, likeCount, false);
    }

    @Override
    @Transactional
    public void deleteMyDocument(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));

        boolean canManage = StpUtil.hasPermission("doc:manage");
        if (!canManage && !Objects.equals(userId, document.getAuthorId())) {
            throw new BizException("只能删除自己的文章");
        }

        removeAttachmentIfNeeded(document.getAttachmentPath());
        documentCommentRepository.deleteByDocumentId(documentId);
        documentLikeRepository.deleteByDocumentId(documentId);
        documentRepository.deleteById(documentId);
    }

    @Override
    public List<DocumentResponse> adminListDocuments(String keyword, DocumentStatus status, Boolean recommended) {
        Long userId = StpUtil.getLoginIdAsLong();
        Specification<Document> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String likeVal = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), likeVal),
                    criteriaBuilder.like(root.get("summary"), likeVal),
                    criteriaBuilder.like(root.get("content"), likeVal)
                ));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (recommended != null) {
                predicates.add(criteriaBuilder.equal(root.get("recommended"), recommended));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        List<Document> documents = documentRepository.findAll(
            specification,
            Sort.by(Sort.Order.desc("recommended"), Sort.Order.desc("updatedAt"))
        );
        return toResponses(documents, userId);
    }

    @Override
    @Transactional
    public DocumentResponse adminUpdateStatus(Long documentId, DocumentStatus status) {
        if (status == null) {
            throw new BizException("状态不能为空");
        }
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));
        document.setStatus(status);
        Document saved = documentRepository.save(document);
        Long userId = StpUtil.getLoginIdAsLong();
        long likeCount = documentLikeRepository.countByDocumentId(saved.getId());
        boolean liked = documentLikeRepository.existsByDocumentIdAndUserId(saved.getId(), userId);
        return toResponse(saved, findAuthorName(saved.getAuthorId()), findTagName(saved.getTagId()), likeCount, liked);
    }

    @Override
    @Transactional
    public DocumentResponse adminUpdateRecommended(Long documentId, boolean recommended) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));
        document.setRecommended(recommended);
        Document saved = documentRepository.save(document);
        Long userId = StpUtil.getLoginIdAsLong();
        long likeCount = documentLikeRepository.countByDocumentId(saved.getId());
        boolean liked = documentLikeRepository.existsByDocumentIdAndUserId(saved.getId(), userId);
        return toResponse(saved, findAuthorName(saved.getAuthorId()), findTagName(saved.getTagId()), likeCount, liked);
    }

    @Override
    @Transactional
    public void adminDeleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BizException("文章不存在"));
        removeAttachmentIfNeeded(document.getAttachmentPath());
        documentCommentRepository.deleteByDocumentId(documentId);
        documentLikeRepository.deleteByDocumentId(documentId);
        documentRepository.deleteById(documentId);
    }

    private List<DocumentResponse> toResponses(List<Document> documents, Long currentUserId) {
        List<Long> authorIds = documents.stream().map(Document::getAuthorId).distinct().toList();
        Map<Long, String> authorMap = authorIds.isEmpty() ? Map.of() : userRepository.findByIdIn(authorIds).stream()
            .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));
        List<Long> tagIds = documents.stream()
            .map(Document::getTagId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> tagMap = tagIds.isEmpty() ? Map.of() : tagRepository.findByIdIn(tagIds).stream()
            .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));
        List<Long> documentIds = documents.stream().map(Document::getId).toList();
        Map<Long, Long> likeCountMap = getLikeCountMap(documentIds);
        Set<Long> likedDocumentIds = getLikedDocumentIds(currentUserId, documentIds);

        return documents.stream()
            .map(d -> {
                Long tagId = d.getTagId();
                String tagName = tagId == null ? null : tagMap.get(tagId);
                Long authorId = d.getAuthorId();
                String authorName = authorId == null ? "未知用户" : authorMap.getOrDefault(authorId, "未知用户");
                return toResponse(
                    d,
                    authorName,
                    tagName,
                    likeCountMap.getOrDefault(d.getId(), 0L),
                    likedDocumentIds.contains(d.getId())
                );
            })
            .toList();
    }

    private String findAuthorName(Long authorId) {
        User author = userRepository.findById(authorId).orElse(null);
        return author == null ? "未知用户" : author.getRealName();
    }

    private String findTagName(Long tagId) {
        if (tagId == null) {
            return null;
        }
        return tagRepository.findById(tagId).map(Tag::getName).orElse(null);
    }

    private void assertReadable(Document document, Long userId) {
        boolean canManage = StpUtil.hasPermission("doc:manage");
        boolean isOwner = Objects.equals(userId, document.getAuthorId());
        boolean isPublished = DocumentStatus.PUBLISHED.equals(document.getStatus());
        if (!isPublished && !isOwner && !canManage) {
            throw new BizException("无权限查看该文章");
        }
    }

    private Map<Long, Long> getLikeCountMap(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> map = new HashMap<>();
        List<Object[]> rows = documentLikeRepository.countGroupedByDocumentIds(documentIds);
        for (Object[] row : rows) {
            if (row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            Long documentId = ((Number) row[0]).longValue();
            Long likeCount = ((Number) row[1]).longValue();
            map.put(documentId, likeCount);
        }
        return map;
    }

    private Set<Long> getLikedDocumentIds(Long userId, List<Long> documentIds) {
        if (userId == null || documentIds == null || documentIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(documentLikeRepository.findLikedDocumentIdsByUserIdAndDocumentIds(userId, documentIds));
    }

    private void removeAttachmentIfNeeded(String attachmentPath) {
        if (attachmentPath == null || attachmentPath.isBlank()) {
            return;
        }
        if (!attachmentPath.startsWith("/uploads/")) {
            return;
        }
        String filename = attachmentPath.substring("/uploads/".length());
        if (filename.isBlank()) {
            return;
        }
        try {
            Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = baseDir.resolve(filename).normalize();
            if (target.startsWith(baseDir)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // ignore attachment cleanup failure, keep delete flow successful
        }
    }

    private Long resolveTagId(Long tagId) {
        if (tagId == null) {
            return null;
        }
        if (!tagRepository.existsById(tagId)) {
            throw new BizException("标签不存在");
        }
        return tagId;
    }

    private DocumentResponse toResponse(Document document, String authorName, String tagName, long likeCount, boolean liked) {
        return DocumentResponse.builder()
            .id(document.getId())
            .title(document.getTitle())
            .summary(document.getSummary())
            .content(document.getContent())
            .attachmentPath(document.getAttachmentPath())
            .authorId(document.getAuthorId())
            .authorName(authorName)
            .semesterTag(document.getSemesterTag())
            .tagId(document.getTagId())
            .tagName(tagName)
            .status(document.getStatus())
            .recommended(Boolean.TRUE.equals(document.getRecommended()))
            .viewCount(document.getViewCount() == null ? 0L : document.getViewCount())
            .likeCount(likeCount)
            .liked(liked)
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .build();
    }
}
