package com.lambda.borme_processor.controller

import com.lambda.borme_processor.dto.BormePublicationDTO
import com.lambda.borme_processor.dto.CompanyDTO
import com.lambda.borme_processor.dto.PaginatedCompaniesDTO
import com.lambda.borme_processor.dto.PaginatedPublicationsDTO
import com.lambda.borme_processor.dto.ProcessingResultDTO
import com.lambda.borme_processor.dto.StatsDTO
import com.lambda.borme_processor.service.BormeProcessorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
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
     * Procesa el BORME de una fecha específica.
     * POST /api/borme/process?date=2025-09-01
     */
    @PostMapping("/process")
    ResponseEntity<ProcessingResultDTO> processBorme(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            ProcessingResultDTO result = processorService.processBormeForDate(date)
            return ResponseEntity.ok(result)
        } catch (Exception e) {
            def errorResult = new ProcessingResultDTO(
                    success: false, message: "Error interno del servidor: ${e.message}", date: date
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult)
        }
    }


    /**
     * Obtiene empresas de una fecha específica (paginado).
     * GET /api/borme/companies?date=2025-09-01&page=0&size=20
     */
    @GetMapping("/companies")
    ResponseEntity<PaginatedCompaniesDTO> getCompaniesByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size)
            Page<CompanyDTO> companiesPage = processorService.findCompaniesByDate(date, pageable)

            def response = new PaginatedCompaniesDTO(
                    success: true, total: companiesPage.totalElements, totalPages: companiesPage.totalPages,
                    currentPage: page, pageSize: size, companies: companiesPage.content
            )
            return ResponseEntity.ok(response)

        } catch (Exception e) {
            def errorResponse = new PaginatedCompaniesDTO(success: false, message: "Error al consultar: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

    /**
     * Obtiene TODAS las empresas (paginado).
     * GET /api/borme/companies/all?page=0&size=20
     */
    @GetMapping("/companies/all")
    ResponseEntity<PaginatedCompaniesDTO> getAllCompanies(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PaginatedCompaniesDTO response = processorService.getAllCompaniesWithMetadata(page, size)
        return ResponseEntity.ok(response)
    }

    /**
     * Obtiene el detalle de UNA empresa por ID.
     * GET /api/borme/companies/123
     */
    @GetMapping("/companies/{id}")
    ResponseEntity<?> getCompanyById(@PathVariable("id") Long id) {
        Optional<CompanyDTO> companyDtoOptional = processorService.findCompanyById(id)

        if (companyDtoOptional.isPresent()) {
            return ResponseEntity.ok(companyDtoOptional.get())
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body([success: false, message: "Empresa no encontrada"])
        }
    }


/**
 * Búsqueda avanzada de empresas con filtros.
 * GET /api/borme/companies/search?name=SL&admin=FERNANDEZ&startDate=2025-01-01&endDate=2025-12-31&page=0&size=20&sort=publication.publicationDate,desc
 */

    @GetMapping("/companies/search")
    ResponseEntity<PaginatedCompaniesDTO> searchCompanies(
            @RequestParam(value = "name", required = false) String name,  // ← Añadir "value"
            @RequestParam(value = "admin", required = false) String admin,
            @RequestParam(value = "solePartner", required = false) String solePartner,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Pageable pageable
            //@RequestParam(value = "sort", defaultValue = "publication.publicationDate,desc") String[] sort
    ) {
        PaginatedCompaniesDTO response = processorService.searchCompaniesWithMetadata(
                name, admin, solePartner, startDate, endDate, pageable
        )
        return ResponseEntity.ok(response)
    }

/**
 * Lista todas las publicaciones.
 */
    @GetMapping("/publications")
    ResponseEntity<PaginatedPublicationsDTO> getPublications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder
    ) {
        PaginatedPublicationsDTO response = processorService.getPublicationsWithMetadata(page, size, sortOrder)
        return ResponseEntity.ok(response)
    }

    /**
     * Lista todas las publicaciones (PDFs procesados) con paginación.
     * GET /api/borme/publications?page=0&size=20

     @GetMapping ("/publications")
      ResponseEntity<PaginatedPublicationsDTO> getPublications(
     @RequestParam (value = "page", defaultValue = "0") int page,
     @RequestParam (value = "size", defaultValue = "20") int size,
     @RequestParam (value = "sortOrder", defaultValue = "desc") String sortOrder
      ) {
      def response = processorService.findAllPublications(page, size, sortOrder)
      return PaginatedCompaniesDTO.ok(response)}
     */
    /**
     * Obtiene estadísticas generales.
     * GET /api/borme/stats
     */
    @GetMapping("/stats")
    ResponseEntity<StatsDTO> getStats() {
        try {
            StatsDTO stats = processorService.getStats()
            return ResponseEntity.ok(stats)
        } catch (Exception e) {
            def errorResponse = new StatsDTO(
                    success: false,
                    message: "Error al obtener estadísticas: ${e.message}"
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
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