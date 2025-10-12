package com.lambda.borme_processor.service

import com.lambda.borme_processor.dto.BormePublicationDTO
import com.lambda.borme_processor.dto.CompanyDTO
import com.lambda.borme_processor.dto.PaginatedCompaniesDTO
import com.lambda.borme_processor.dto.PaginatedPublicationsDTO
import com.lambda.borme_processor.dto.ProcessingResultDTO
import com.lambda.borme_processor.dto.StatsDTO
import com.lambda.borme_processor.entity.BormePublication
import com.lambda.borme_processor.entity.Company
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BormeProcessorService {

    @Autowired
    private BormeScraperService scraperService
    @Autowired
    private PdfParsingService pdfParsingService
    @Autowired
    private BormeParserService bormeParserService
    @Autowired
    private PersistenceService persistenceService

    /**
     * Servicio principal que orquesta el flujo completo del procesamiento del BORME.
     *
     * Responsabilidades:
     *  - Coordinar la descarga de PDFs (BormeScraperService)
     *  - Extraer texto de los PDFs (PdfParsingService)
     *  - Parsear el texto en entidades estructuradas (BormeParserService)
     *  - Persistir los datos obtenidos (PersistenceService)
     *
     * @param date La fecha que se usara para obtener datos del Borme.
     * @return Un`ProcessingResultDTO` que contiene el resultado de procesar el Borme para esa fecha.
     */
    ProcessingResultDTO processBormeForDate(LocalDate date) {
        println "[MANDO] Iniciando operación para la fecha: $date"

        // Scraper, adquiera los activos.
        def downloadedFiles = scraperService.scrapeAndDownloadPdfs(date)

        // Si no hay activos, abortar y reportar.
        if (downloadedFiles.isEmpty()) {
            return new ProcessingResultDTO(
                    success: false,
                    message: "No se encontraron publicaciones BORME para la fecha: $date",
                    date: date,
                    filesProcessed: 0,
                    companiesFound: 0
            )
        }

        int totalCompaniesFound = 0
        List<String> collectedUrls = []

        // 3. Procesar cada activo asegurado.
        downloadedFiles.each { scrapedFile ->
            // Parser de PDF, extrae la inteligencia en bruto.
            String text = pdfParsingService.extractTextFromFile(scrapedFile.localFile)

            // Parser de Texto, estructura la inteligencia.
            def companies = bormeParserService.extractCompaniesFromText(text, scrapedFile.localFile.name)

            // Guarda la URL pública.
            collectedUrls.add(scrapedFile.publicUrl)

            // Guarda en base de datos.
            persistenceService.saveBormeData(
                    scrapedFile.localFile.name,
                    date,
                    scrapedFile.publicUrl,
                    companies
            )
            totalCompaniesFound += companies.size()
        }

        println "[MANDO] Operación completada para la fecha: $date"

        // Construye el DTO de resultado con toda la inteligencia recopilada.
        return new ProcessingResultDTO(
                success: true,
                message: "Procesamiento completado con éxito",
                date: date,
                filesProcessed: downloadedFiles.size(),
                companiesFound: totalCompaniesFound,
                fileUrls: collectedUrls
        )
    }
    /**
     * Obtiene todas las compañías y las convierte a DTOs.
     */
    /**
     * Retrieves all companies and converts them to DTOs.
     *
     * @param pageable Los parametros de paginacion
     * @return Una lista de objetos `CompanyDTO` con datos de compañias.
     */
    Page<CompanyDTO> findAllCompanies(Pageable pageable) {
        Page<Company> companyPage = persistenceService.findAllCompanies(pageable)
        return companyPage.map { company -> convertToDto(company) }
    }

    /**
     * Obtiene compañías por fecha y las convierte a DTOs.
     */
    /**
     * Retrieves companies by a specific date and converts them to DTOs.
     *
     * @param date La fecha que se usará para encontrar las companias
     * @param pageable Los parametros de paginacion
     * @return Una lista de objetos `CompanyDTO` con datos de compañias.
     */
    Page<CompanyDTO> findCompaniesByDate(LocalDate date, Pageable pageable) {
        Page<Company> companyPage = persistenceService.findCompaniesByDate(date, pageable)
        return companyPage.map { company -> convertToDto(company) }
    }

    /**
     * Obtiene una compañía por ID y la convierte a DTO.
     */
    /**
     * Retrieves a company by its ID and converts it to a DTO.
     *
     * @param id El ID de la compania a mostrar.
     * @return Un `Optional` conteniendo un `CompanyDTO` con datos de la compañia, si se ha encontrado, o vacio.
     */
    Optional<CompanyDTO> findCompanyById(Long id) {
        Optional<Company> companyOptional = persistenceService.findCompanyById(id)
        return companyOptional.map { company -> convertToDto(company) }
    }

    /**
     * Busca compañías que cumplan con los filtros especificados.
     * Permite realizar consultas dinámicas por nombre, administrador, socio único
     * y por rango de fechas de publicación. Devuelve los resultados paginados.
     *
     * @param name Nombre (o parte del nombre) de la compañía a buscar.
     * @param admin Nombre del administrador asociado.
     * @param solePartner Nombre del socio único asociado.
     * @param startDate Fecha inicial del rango de publicación (inclusive).
     * @param endDate Fecha final del rango de publicación (inclusive).
     * @param pageable Parámetros de paginación (página y tamaño de resultados).
     * @return Página de `CompanyDTO` con compañías que coinciden con los filtros aplicados.
     */
    PaginatedCompaniesDTO searchCompaniesWithMetadata(
            String name,
            String admin,
            String solePartner,
            String startDate,
            String endDate,
            Pageable pageable
    ) {
        try {
            Page<Company> companyPage = persistenceService.searchCompaniesWithMetadata(
                    name, admin, solePartner, startDate, endDate, pageable
            )

            def companyDTOs = companyPage.content.collect { company -> convertToDto(company) }

            return new PaginatedCompaniesDTO(
                    success: true,
                    total: companyPage.totalElements,
                    totalPages: companyPage.totalPages,
                    currentPage: pageable.getPageNumber(),
                    pageSize: pageable.getPageSize(),
                    companies: companyDTOs
            )
        } catch (Exception e) {
            return new PaginatedCompaniesDTO(
                    success: false,
                    message: "Error: ${e.message}"
            )
        }
    }

/**
 * Obtiene y empaqueta una respuesta paginada de todas las publicaciones.
 * @param pageable Los parámetros de paginación.
 * @return Un DTO que contiene la respuesta paginada completa.
 */
    PaginatedPublicationsDTO findAllPublications(Pageable pageable) {
        Page<BormePublication> publicationPage = persistenceService.findAllPublications(pageable)

        def publicationDTOs = publicationPage.content.collect { publication ->
            new BormePublicationDTO(
                    id: publication.getId(),
                    publicationDate: publication.getPublicationDate(),
                    fileName: publication.getFileName(),
                    fileUrl: publication.getFileUrl()
            )
        }

        return new PaginatedPublicationsDTO(
                success: true,
                total: publicationPage.totalElements,
                totalPages: publicationPage.totalPages,
                currentPage: pageable.getPageNumber(),
                pageSize: pageable.getPageSize(),
                publications: publicationDTOs
        )
    }

    /**
     * Recopila y prepara las estadísticas generales de la aplicación.
     * @return Un DTO con las estadísticas compiladas.
     */
    StatsDTO getStats() {
        // Recopilar datos brutos.
        def totalCompanies = persistenceService.countTotalCompanies()
        def totalPublications = persistenceService.countTotalPublications()
        Optional<LocalDate> latestDateOptional = persistenceService.findLatestPublicationDate()

        // Procesa y formatea los datos.
        def latestDateString = latestDateOptional.map({ it.toString() }).orElse(null)

        // Construye y devuelve el DTO.
        return new StatsDTO(
                success: true,
                totalCompanies: totalCompanies,
                totalPublications: totalPublications,
                latestDate: latestDateString
        )
    }

    // --- UNIDAD INTERNA ---
    /**
     * Método privado y reutilizable para convertir una Entidad Company a un CompanyDTO.
     * @param company La `Company` entity a convertir.
     * @return El correspondiente`CompanyDTO`.
     */
    private static CompanyDTO convertToDto(Company company) {
        return new CompanyDTO(
                id: company.getId(),
                bormeId: company.getBormeId(),
                name: company.getName(),
                actType: company.getActType(),
                startDate: company.getStartDate(),
                object: company.getObject(),
                address: company.getAddress(),
                capital: company.getCapital(),
                solePartner: company.getSolePartner(),
                admin: company.getAdmin(),
                registryData: company.getRegistryData(),
                publicationId: company.getPublication().getId()
        )
    }
}
