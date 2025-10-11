package com.lambda.borme_processor.entity

import groovy.transform.Canonical
import jakarta.persistence.*
import lombok.Data

import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "borme_publications")
//@Data
//@Canonical // <-- Reemplaza a @Data. Es idiomático en Groovy.
class BormePublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(name = "publication_date", nullable = false)
    LocalDate publicationDate

    @Column(name = "file_name", nullable = false, unique = true)
    String fileName

    @Column(name = "file_url", nullable = false, unique = true)
    private String pdfPath // <<--- referencia al PDF original

    @Column(name = "processed_at", nullable = false)
    OffsetDateTime processedAt = OffsetDateTime.now()

    // --- CONSTRUCTOR VACÍO (Requerido por JPA) ---
    BormePublication() {}

    // --- GETTERS FORJADOS A MANO ---
    Long getId() { return id }

    LocalDate getPublicationDate() { return publicationDate }

    String getFileName() { return fileName }

    String getFileUrl() { return pdfPath }

    OffsetDateTime getProcessedAt() { return processedAt }
}