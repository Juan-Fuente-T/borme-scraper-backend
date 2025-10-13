package com.lambda.borme_processor.repository

import com.lambda.borme_processor.entity.BormePublication
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

@Repository
interface BormePublicationRepository extends JpaRepository<BormePublication, Long> {
    Optional<BormePublication> findByFileName(String fileName)

    Optional<BormePublication> findTopByOrderByPublicationDateDesc()

    // Búsqueda por nombre de archivo
    //Page<BormePublication> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable)

    // Búsqueda por rango de fechas
    Page<BormePublication> findByPublicationDateBetween(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    )
    // Devuelve si existe una publicación para una fecha
    boolean existsByPublicationDate(LocalDate publicationDate)

    // Borrado de publicaciones por fecha
    @Modifying
    @Transactional
    long deleteByPublicationDate(LocalDate publicationDate)
}