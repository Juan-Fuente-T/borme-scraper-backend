package com.lambda.borme_processor.controller

import com.lambda.borme_processor.dto.CompanyDTO
import com.lambda.borme_processor.dto.PaginatedCompaniesDTO
import com.lambda.borme_processor.dto.PaginatedPublicationsDTO
import com.lambda.borme_processor.dto.ProcessingResultDTO
import com.lambda.borme_processor.dto.StatsDTO
import com.lambda.borme_processor.service.BormeProcessorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Sort

import java.time.LocalDate

/**
 * Controlador API REST.
 * Expone los endpoints HTTP y delega toda la lógica de negocio al BormeProcessorService.
 */
@RestController
@RequestMapping("/api/borme")
@CrossOrigin(origins = "*")
class BormeController {

    @Autowired
    private BormeProcessorService processorService

    /**
     * Endpoint para procesar el BORME de una fecha específica.
     *
     * POST /api/borme/process?date=2025-09-01
     *
     * @param date La fecha del BORME que se desea procesar (formato: yyyy-MM-dd).
     * @return Un `ResponseEntity` que contiene un `ProcessingResultDTO` con
     *                        la lista de empresas resultado del procesamiento.
     */

    @PostMapping("/process")
    ResponseEntity<ProcessingResultDTO> processBorme(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) {
        ProcessingResultDTO result = processorService.processBormeForDate(date, force)
        return ResponseEntity.ok(result)
    }

    /**
     * Obtiene y empaqueta una respuesta paginada de compañías para una fecha específica.
     * GET /api/borme/companies?date=2025-09-01&page=0&size=20
     * @param date La fecha de la publicación.
     * @param pageable Los parámetros de paginación.
     * @return Un DTO que contiene la respuesta paginada completa.
     */
    @GetMapping("/companies")
    ResponseEntity<PaginatedCompaniesDTO> getCompaniesByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            // Spring construye el Pageable a partir de los parámetros ?page, ?size y ?sort.
            Pageable pageable
    ) {
        PaginatedCompaniesDTO response = processorService.findCompaniesByDate(date, pageable)
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para obtener todas las empresas con paginación.
     *
     * GET /api/borme/companies/all?page=0&size=20
     *
     * @param pageable Parámetros de paginación, incluyendo:
     *                 - page: Número de página (por defecto 0).
     *                 - size: Tamaño de la página (por defecto 20).
     *                 - sort: Campo y dirección de ordenación (por defecto `startDate,desc`).
     * @return Un `ResponseEntity` que contiene un `PaginatedCompaniesDTO` con las empresas paginadas.
     */
    //@GetMapping("/companies/all")
    //    ResponseEntity<PaginatedCompaniesDTO> getAllCompanies(
    //            @RequestParam(value = "page", defaultValue = "0") int page,
    //            @RequestParam(value = "size", defaultValue = "20") int size
    //    ) {
    @GetMapping("/companies/all")
    ResponseEntity<PaginatedCompaniesDTO> getAllCompanies(
            @PageableDefault(page = 0, size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PaginatedCompaniesDTO response = processorService.findAllCompanies(pageable)
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para obtener el detalle de una empresa por su ID.
     *
     * GET /api/borme/companies/123
     *
     * @param id El ID de la empresa que se desea obtener.
     * @return Un `ResponseEntity` que contiene un `CompanyDTO` con los detalles de la empresa.
     *         Si no se encuentra la empresa, se lanza una excepción.
     */
    @GetMapping("/companies/{id}")
    ResponseEntity<?> getCompanyById(@PathVariable("id") Long id) {
        CompanyDTO companyDto = processorService.findCompanyById(id)
        return ResponseEntity.ok(companyDto)
    }

    /**
     * Endpoint para realizar una búsqueda avanzada de empresas con filtros.
     *
     * GET /api/borme/companies/search
     * GET /api/borme/companies/search?name=SL&admin=FERNANDEZ&startDate=2025-01-01&endDate=2025-12-31&page=0&size=20&sort=publication.publicationDate,desc
     *
     * @param name Opcional. El nombre de la empresa a buscar.
     * @param admin Opcional. El nombre del administrador para filtrar.
     * @param solePartner Opcional. Indica si la empresa tiene un socio único.
     * @param startDate Opcional. La fecha de inicio para filtrar empresas (formato: yyyy-MM-dd).
     * @param endDate Opcional. La fecha de fin para filtrar empresas (formato: yyyy-MM-dd).
     * @param pageable Parámetros de paginación, incluyendo:
     *                 - page: Número de página (por defecto 0).
     *                 - size: Tamaño de la página (por defecto 20).
     *                 - sort: Campo y dirección de ordenación (por ejemplo, `startDate,desc`).
     * @return Un `ResponseEntity` que contiene un `PaginatedCompaniesDTO` con los resultados paginados de la búsqueda.
     */
    @GetMapping("/companies/search")
    ResponseEntity<PaginatedCompaniesDTO> searchCompanies(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "admin", required = false) String admin,
            @RequestParam(value = "solePartner", required = false) String solePartner,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Pageable pageable
    ) {
        PaginatedCompaniesDTO response = processorService.searchCompaniesWithMetadata(
                name, admin, solePartner, startDate, endDate, pageable
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para listar todas las publicaciones procesadas con paginación.
     *
     * GET /api/borme/publications
     *
     * @param pageable Parámetros de paginación, incluyendo:
     *                 - page: Número de página (por defecto 0).
     *                 - size: Tamaño de la página (por defecto 30).
     *                 - sort: Campo por el cual ordenar (por defecto "publicationDate").
     *                 - direction: Dirección del orden (por defecto DESC).
     * @return ResponseEntity que contiene un objeto `PaginatedPublicationsDTO` con:
     *         - success: Indica si la operación fue exitosa.
     *         - total: Número total de publicaciones.
     *         - totalPages: Número total de páginas.
     *         - currentPage: Página actual.
     *         - pageSize: Tamaño de la página.
     *         - publications: Lista de publicaciones en la página actual.
     */
    @GetMapping("/publications")
    ResponseEntity<PaginatedPublicationsDTO> getPublications(
            // Anotación para establecer valores por defecto si no vienen en la URL.
            @PageableDefault(page = 0, size = 30, sort = "publicationDate", direction = Sort.Direction.DESC)
                    Pageable pageable
    ) {
        // Da la orden al servicio que finalmente llama al repository
        PaginatedPublicationsDTO response = processorService.findAllPublications(pageable)

        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para obtener el archivo PDF de una publicación específica.
     *
     * GET /api/borme/publications/proxy/{id}
     * @param id El ID de la publicación cuyo PDF se desea obtener.
     * @return Un `ResponseEntity` que contiene los bytes del archivo PDF y los encabezados HTTP correspondientes.
     *         Si no se encuentra el archivo, devuelve un cuerpo nulo con el estado HTTP 200.
     */
    @GetMapping("/publications/proxy/{id}")
    ResponseEntity<byte[]> getPublicationPdf(@PathVariable("id") Long id) {
        byte[] pdfBytes = processorService.getPublicationPdfBytes(id)

        // Si no tiene éxito se lanza excepción en el servicio
        HttpHeaders headers = new HttpHeaders()
        headers.add("Content-Type", "application/pdf")
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK)
    }

    /**
     * Endpoint para obtener estadísticas generales del sistema.
     *
     * GET /api/borme/stats
     * @return Un `ResponseEntity` que contiene un objeto `StatsDTO` con las estadísticas generales si la operación es exitosa.
     *         En caso de error, devuelve un `StatsDTO` con un mensaje de error y el estado HTTP 500 (Error interno del servidor).
     */
    @GetMapping("/stats")
    ResponseEntity<StatsDTO> getStats() {
        StatsDTO stats = processorService.getStats()
        return ResponseEntity.ok(stats)
    }

    /**
     * Health check del API.
     * GET /api/borme/health
     */
    @GetMapping("/health")
    static ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok([
                status : "UP",
                message: "BORME API funcionando correctamente"
        ])
    }
}