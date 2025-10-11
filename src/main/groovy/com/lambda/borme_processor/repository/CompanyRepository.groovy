package com.lambda.borme_processor.repository

import com.lambda.borme_processor.entity.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

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

    // Búsqueda combinada: texto + rango de fechas
    //@Query("""
    //        SELECT c FROM Company c
    //        WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
    //        AND (:startDate IS NULL OR c.publication.publicationDate >= :startDate)
    //        AND (:endDate IS NULL OR c.publication.publicationDate <= :endDate)
    //    """)
    //    Page<Company> searchCompanies(
    //            @Param("name") String name,
    //            @Param("startDate") LocalDate startDate,
    //            @Param("endDate") LocalDate endDate,
    //            Pageable pageable
    //    )

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
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    )
}

//AND (:startDate IS NULL OR c.publication.publicationDate >= :startDate)
//        AND (:endDate IS NULL OR c.publication.publicationDate <= :endDate)