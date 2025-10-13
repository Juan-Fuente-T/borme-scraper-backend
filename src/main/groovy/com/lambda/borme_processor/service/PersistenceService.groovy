package com.lambda.borme_processor.service

import com.lambda.borme_processor.entity.BormePublication
import com.lambda.borme_processor.entity.Company
import com.lambda.borme_processor.repository.BormePublicationRepository
import com.lambda.borme_processor.repository.CompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

/**
 * Servicio para manejar la persistencia de datos relacionados con el BORME.
 * Incluye operaciones para guardar publicaciones y compañías, así como consultas.
 */
@Service
class PersistenceService {

    @Autowired
    private BormePublicationRepository publicationRepository

    @Autowired
    private CompanyRepository companyRepository

    /**
     * Guarda los datos del BORME en la base de datos.
     * @param fileName Nombre del fichero PDF.
     * @param publicationDate Fecha de publicación del BORME.
     * @param fileUrl URL o ruta del fichero PDF.
     * @param companies Lista de compañías extraídas del PDF.
     */
    @Transactional
    // Asegura que toda la operación sea atómica.
    void saveBormeData(String fileName, LocalDate publicationDate, String fileUrl, List<Company> companies) {
        println "[LOGÍSTICA] Iniciando protocolo de persistencia para el fichero: $fileName"

        // 1. Crear y guardar el registro de la publicación
        BormePublication publication = new BormePublication(
                publicationDate: publicationDate,
                fileName: fileName,
                pdfPath: fileUrl
        )

        BormePublication savedPublication = publicationRepository.save(publication)
        println "[LOGÍSTICA] Publicación registrada con ID: ${savedPublication.id}"

        // 2. Vincular cada compañía a la publicación y guardarlas
        companies.each { company ->
            company.publication = savedPublication
        }
        companyRepository.saveAll(companies)
        println "[LOGÍSTICA] ${companies.size()} registros de compañía persistidos con éxito."
    }

    /**
     * Encuentra compañías por fecha de publicación. Devuelve ENTIDADES.
     * @param pageable    Parámetros de paginación (página y tamaño de resultados).
     * @return    Todas las compañias paginadas para un día concreto
     */
    Page<Company> findCompaniesByDate(LocalDate date, Pageable pageable) {
        return companyRepository.findByPublicationPublicationDate(date, pageable)
    }

    /**
     * Encuentra todas las compañías. Devuelve ENTIDADES.
     * @param pageable    Parámetros de paginación (página y tamaño de resultados).
     * @return    Todas las compañías paginadas
     */
    Page<Company> findAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
    }

    /**
     * Encuentra una compañía por su ID. Devuelve una ENTIDAD.
     * @param id ID de la compñia a encontrar
     * @return    La compañia correspondiente a ese ID
     */
    Optional<Company> findCompanyById(Long id) {
        return companyRepository.findById(id)
    }

    /**
     * Encuentra todas las publicaciones. Devuelve ENTIDADES.
     * @param pageable    Parámetros de paginación (página y tamaño de resultados).
     * @return    Todas las publicaciones paginadas
     */
    Page<BormePublication> findAllPublications(Pageable pageable) {
        return publicationRepository.findAll(pageable)
    }

    /**
     * Cuenta el total de compañías.
     * @return    La cantidad total de compañías.
     */
    Long countTotalCompanies() {
        return companyRepository.count()
    }

    /**
     * Cuenta el total de publicaciones.
     * @return    La cantidad total de publicaciones.
     */
    Long countTotalPublications() {
        return publicationRepository.count()
    }

    /**
     * Encuentra la fecha de la publicación más reciente.
     * @return    Fecha de publicación del ultimo documento .
     */
    Optional<LocalDate> findLatestPublicationDate() {
        Optional<BormePublication> latestPublicationOptional = publicationRepository.findTopByOrderByPublicationDateDesc()
        return latestPublicationOptional.map({ publication -> publication.getPublicationDate() })
    }

    /**
     * Busca compañías que cumplan con los filtros especificados.
     * Permite realizar consultas dinámicas por nombre, administrador, socio único
     * y por rango de fechas de publicación. Devuelve los resultados paginados.
     *
     * @param name        Nombre (o parte del nombre) de la compañía a buscar.
     * @param admin       Nombre del administrador asociado.
     * @param solePartner Nombre del socio único asociado.
     * @param startDate   Fecha inicial del rango de publicación (inclusive).
     * @param endDate     Fecha final del rango de publicación (inclusive).
     * @param pageable    Parámetros de paginación (página y tamaño de resultados).
     * @return            Página de compañías que coinciden con los filtros aplicados.
     */
    Page<Company> searchCompaniesWithMetadata(
            String name,
            String admin,
            String solePartner,
            String startDate,
            String endDate,
            Pageable pageable
    ) {
        return companyRepository.searchCompanies(name, admin, solePartner, startDate, endDate, pageable)
    }

    /**
     * Encuentra una publicación por su ID. Devuelve una ENTIDAD.
     * @param id El ID de la BormePublication a buscar.
     * @return Un Optional que contiene la entidad si se encuentra.
     */
    Optional<BormePublication> findPublicationById(Long id) {
        return publicationRepository.findById(id)
    }

    /**
     * Verifica si existen datos para una fecha específica.
     *
     * @param date La fecha para la cual se desea verificar la existencia de datos.
     * @return `true` si existen datos para la fecha especificada, `false` en caso contrario.
     */
    boolean doesDataExistForDate(LocalDate date) {
        println "[PERSISTENCIA] Verificando existencia de datos para la fecha: ${date}"
        return publicationRepository.existsByPublicationDate(date)
    }

    /**
     * Borra publicaciones y todas las asociados para una fecha en especídico.
     *
     * @param date The date for which the publications and associated data should be deleted.
     */
    void deletePublicationsAndAssociatedDataByDate(LocalDate date) {
        println "[PERSISTENCIA] Iniciando purga en dos fases para la fecha: ${date}"

        // Eliminar las compañías relacionadas.
        println "[PERSISTENCIA] Fase 1: Ordenando eliminación de compañías..."
        long deletedCompanies = companyRepository.deleteByPublication_PublicationDate(date)
        println "[PERSISTENCIA] -> ${deletedCompanies} compañías eliminadas."

        //  Eliminar las publicaciones.
        println "[PERSISTENCIA] Fase 2: Ordenando eliminación de publicaciones..."
        long deletedPublications = publicationRepository.deleteByPublicationDate(date)
        println "[PERSISTENCIA] -> ${deletedPublications} publicaciones eliminadas."

        println "[PERSISTENCIA] Purga completada con éxito."
    }
}