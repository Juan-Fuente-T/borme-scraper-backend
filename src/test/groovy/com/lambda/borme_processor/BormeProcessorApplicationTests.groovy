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
// Elimina la autoconfiguración de la base de datos y Flyway para este test.
@EnableAutoConfiguration(
        exclude = [DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class]
)
@ActiveProfiles("test")
class BormeProcessorApplicationTests {

    @Autowired
    private PdfParsingService pdfParsingService
    @Autowired
    private BormeParserService bormeParserService

    // @MockBean es la anotación correcta, aunque algunos IDEs la marquen como obsoleta,
    // para esta configuración es adecuada y funcional.
    @MockBean
    private BormeScraperService scraperService

    @Test
    void contextLoadsAndFullProcessRuns() {
        // --- ADQUISICIÓN DEL OBJETIVO REAL ---
        // Se carga el fichero BORME completo desde los recursos de prueba.
        def bormePdfResource = new ClassPathResource("BORME-A-2025-165-03.pdf")
        def bormeFile = bormePdfResource.getFile()

        // --- EJECUCIÓN DE LA OPERACIÓN COMPLETA ---
        // 1. Se extrae el texto del PDF real.
        String fullText = pdfParsingService.extractTextFromFile(bormeFile)
        assert fullText != null && !fullText.isBlank()

        // 2. Se procesa el texto extraído para obtener las entidades Company.
        def companies = bormeParserService.extractCompaniesFromText(fullText, bormeFile.getName())

        // --- VERIFICACIÓN ---
        println "[TEST] Parseo completado. Se han extraído ${companies.size()} registros de 'Constitución'."
        assert companies.size() > 0 // Verificación básica de que se ha extraído al menos una empresa.

        // [ANÁLISIS ESTRATÉGICO] Añadir aserciones específicas para verificar datos conocidos del fichero real.
        // Ejemplo:
        // def specificCompany = companies.find { it.bormeId == '389835' }
        // assert specificCompany != null
        // assert specificCompany.name == "TRIUNFO IBERICO SL"
         def specificCompany = companies.find { it.bormeId == '389835' }
         assert specificCompany != null
         assert specificCompany.name == "TRIUNFO IBERICO SL"
         assert specificCompany.object == "Restaurantes y puestos de comidas"
         assert specificCompany.capital == "3.000,00 Euros"
         assert specificCompany.admin == "LILLO MARTINEZ VICENTE"
         assert specificCompany.registryData == "S 8 , H A 199944, I/A 1 (25.08.25)"
        println "[TEST DE INTEGRACIÓN] Simulación completada con éxito. El flujo de análisis funciona."
    }
}

