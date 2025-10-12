package com.lambda.borme_processor.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.Canonical
import jakarta.persistence.*
import lombok.Data

import java.time.LocalDateTime

@Entity
@Table(name = "company")
//@Data
//@Canonical // <-- Reemplaza a @Data. Es idiomático en Groovy.
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id

    @Column(name = "borme_id", nullable = false)
    private String bormeId

    @Column(name = "name", nullable = false)
    private String name

    @Column(name = "act_type")
    private String actType

    @Column(name = "start_date")
    private String startDate

    @Column(name = "object")
    private String object

    @Column(name = "address")
    private String address

    @Column(name = "capital")
    private String capital

    @Column(name = "capital_numeric")
    private Long capitalNumeric // El valor numérico en céntimos

    @Column(name = "sole_partner")
    private String solePartner

    @Column(name = "admin")
    private String admin

    @Column(name = "registry_data")
    private String registryData

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", nullable = false) // Clave foránea
    private BormePublication publication

    // --- CONSTRUCTOR VACÍO (Requerido por JPA) ---
    Company() {}

    // --- GETTERS FORJADOS A MANO ---
    Long getId() {
        return id
    }

    String getBormeId() {
        return bormeId
    }

    String getName() {
        return name
    }

    String getActType() {
        return actType
    }

    String getStartDate() {
        return startDate
    }

    String getObject() {
        return object
    }

    String getAddress() {
        return address
    }

    String getCapital() {
        return capital
    }

    Long getCapitalNumeric() {
        return capitalNumeric;
    }

    String getSolePartner() {
        return solePartner
    }

    String getAdmin() {
        return admin
    }

    String getRegistryData() {
        return registryData
    }

    LocalDateTime getCreatedAt() {
        return createdAt
    }

    BormePublication getPublication() {
        return publication
    }
}


