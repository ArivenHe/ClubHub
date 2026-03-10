package com.clubhub.entity;

import com.clubhub.enums.SoftwareToolStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "biz_software_tool")
public class SoftwareTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 80)
    private String category;

    @Column(length = 255)
    private String downloadUrl;

    @Column(length = 500)
    private String description;

    @Column(length = 80)
    private String recommendedBy;

    @Column
    private Long applicantId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SoftwareToolStatus status;

    @Column(length = 255)
    private String reviewRemark;

    @Column
    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SoftwareToolStatus.PENDING;
        }
    }
}
