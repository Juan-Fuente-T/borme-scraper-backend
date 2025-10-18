package com.lambda.borme_processor.entity

import jakarta.persistence.*

import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "borme_publications")
//@Data
//@Canonical // <-- Reemplaza a @Data. Es idiomático en Groovy.
class BormePublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id

    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate

    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName

    @Column(name = "file_url", nullable = false, unique = true)
    private String pdfPath // <<--- referencia al PDF original

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now()

    // --- CONSTRUCTOR VACÍO (Requerido por JPA) ---
    BormePublication() {}

    // --- GETTERS FORJADOS A MANO ---
    Long getId() { return id }

    LocalDate getPublicationDate() { return publicationDate }

    String getFileName() { return fileName }

    String getFileUrl() { return pdfPath }

    LocalDateTime getProcessedAt() { return processedAt }
}