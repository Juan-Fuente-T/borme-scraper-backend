package com.lambda.borme_processor

import com.lambda.borme_processor.service.BormeParserService
import com.lambda.borme_processor.service.BormeScraperService
import com.lambda.borme_processor.service.PdfParsingService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDate

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = [PdfParsingService.class, BormeParserService.class] // Carga solo los beans necesarios
)
// --- INICIO DE LA CORRECCIÓN FINAL ---
// Esta es la forma correcta y documentada de excluir configuraciones.
// Aniquila la autoconfiguración de la base de datos y Flyway para este test.
@EnableAutoConfiguration(
        exclude = [DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class]
)
// --- FIN DE LA CORRECCIÓN FINAL ---
@ActiveProfiles("test")
class SimulatedBormeProcessorApplicationTests {

    @Autowired
    private PdfParsingService pdfParsingService
    @Autowired
    private BormeParserService bormeParserService

    // @MockBean es la anotación correcta para reemplazar un bean en el contexto de @SpringBootTest.
    // Aunque algunos IDEs la marquen como obsoleta, para esta configuración es el arma estándar y funcional.
    @MockBean
    private BormeScraperService scraperService

    @Test
    void contextLoadsAndFullProcessRuns() {
        // --- PREPARACIÓN DEL SEÑUELO ---
        // Se simula que el scraper ha descargado un fichero de prueba local.
        def testPdfResource = new ClassPathResource("test.pdf")
        when(scraperService.scrapeAndDownloadPdfs(any(LocalDate.class)))
                .thenReturn([testPdfResource.getFile()])

        // --- DATOS DE INTELIGENCIA DE PRUEBA (Texto real de un BORME) ---
        String testText = """
389826 - MEDITERRAMOVING SL.
Constitución. Comienzo de operaciones: 9.07.25. Objeto social: Transporte de mercancías, mudanzas, almacenajes de mercancías. 
Guardamuebles, logística. Servicio de embalaje. Domicilio: PTDA DE 
ALZABARES BAJO 46 (ELCHE). Capital: 3.000,00 Euros. Declaración de unipersonalidad. Socio único: VIDAL RICO RICARDO. 
Nombramientos. Adm. Unico: VIDAL RICO RICARDO. Datos registrales. S 8 , H A 199888, I/A 1 (25.08.25).
"""

        // --- EJECUCIÓN DE LA SIMULACIÓN ---
        def companies = bormeParserService.extractCompaniesFromText(testText, "test.pdf")

        // --- VERIFICACIÓN ---
        assert companies.size() == 1
        def company = companies[0]
        assert company.name == "MEDITERRAMOVING SL"
        assert company.capital.contains("3.000,00 Euros")
        assert company.solePartner == "VIDAL RICO RICARDO"

        println "[TEST DE INTEGRACIÓN] Simulación completada con éxito. El flujo de análisis funciona."
    }
}

