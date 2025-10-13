package com.lambda.borme_processor.repository

import com.lambda.borme_processor.entity.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

@Repository
interface CompanyRepository extends JpaRepository<Company, Long> {
    // Búsqueda por fecha de publicación
    Page<Company> findByPublicationPublicationDate(LocalDate date, Pageable pageable)

    // Búsqueda por nombre (contiene texto)
    //Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable)

    // Búsqueda por rango de fechas
    Page<Company> findByPublicationPublicationDateBetween(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    )

    // Borrado de publicaciones por fecha
    @Modifying
    @Transactional
    long deleteByPublication_PublicationDate(LocalDate publicationDate)

    // Búsqueda avanzada (nombre, admin o inversor) con + rango de fechas
    @Query(""" 
        SELECT c FROM Company c 
        WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:admin IS NULL OR LOWER(c.admin) LIKE LOWER(CONCAT('%', :admin, '%')))
        AND (:solePartner IS NULL OR LOWER(c.solePartner) LIKE LOWER(CONCAT('%', :solePartner, '%')))
        AND (:startDate IS NULL OR c.startDate >= :startDate)
        AND (:endDate IS NULL OR c.startDate <= :endDate)
    """)
    Page<Company> searchCompanies(
            @Param("name") String name,
            @Param("admin") String admin,
            @Param("solePartner") String solePartner,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable
    )
}
