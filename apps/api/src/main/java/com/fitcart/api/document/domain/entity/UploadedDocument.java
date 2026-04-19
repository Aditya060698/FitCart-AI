package com.fitcart.api.document.domain.entity;

import com.fitcart.api.common.domain.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "uploaded_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedDocument extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String userReference;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 120)
    private String mimeType;

    private Long fileSizeBytes;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(length = 80)
    private String documentType;

    @Column(nullable = false, length = 40)
    private String processingStatus;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(length = 1000)
    private String processingErrorMessage;

    private OffsetDateTime uploadedAt;

    private OffsetDateTime processedAt;
}
