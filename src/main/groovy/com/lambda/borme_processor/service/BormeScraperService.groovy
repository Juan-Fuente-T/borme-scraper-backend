// package com.lambda.borme_processor
package com.lambda.borme_processor.service

import com.lambda.borme_processor.dto.ScrapedFileInfoDTO
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import java.time.LocalDate

@Service
class BormeScraperService {

    private final String BORME_URL_TEMPLATE = "https://www.boe.es/borme/dias/%d/%02d/%02d/"

    /**
     * Orquesta el proceso de scraping y descarga de PDFs del BORME para una fecha específica.
     * Gestiona un directorio temporal para los ficheros y devuelve una lista de los mismos.
     * @param targetDate La fecha objetivo para el scraping.
     * @return Una lista de los ficheros PDF descargados.
     */
    List<ScrapedFileInfoDTO> scrapeAndDownloadPdfs(LocalDate targetDate) {
        println "[UNIDAD DE ADQUISICIÓN] Iniciando operación para la fecha: ${targetDate}..."
        // Se gestiona un directorio temporal, específico para la fecha.
        def downloadDir = new File("temp_borme_${targetDate}")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        // Se crea la lista que contendrá los paquetes descargados.
        List<ScrapedFileInfoDTO> scrapedIntelligence = []

        // Construye la URL dinámicamente a partir de la fecha.
        String bormeUrl = String.format(BORME_URL_TEMPLATE, targetDate.getYear(), targetDate.getMonthValue(), targetDate.getDayOfMonth())

        try {
            SSLContext sslContext = createSslContext()

            println "[UNIDAD DE ADQUISICIÓN] Extrayendo lista de objetivos de ${bormeUrl}..."
            Document doc = Jsoup.connect(bormeUrl).sslSocketFactory(sslContext.getSocketFactory()).get()
            //def pdfLinks = doc.select('a[href$=.pdf]')
            def pdfLinks = doc.select('a[href*="BORME-A-"]:not([href$="-99.pdf"])')
            println "[UNIDAD DE ADQUISICIÓN] Se han identificado ${pdfLinks.size()} objetivos."

            if (pdfLinks.isEmpty()) {
                println "[UNIDAD DE ADQUISICIÓN] Cero objetivos encontrados. Misión completada."
                return Collections.emptyList() // Devuelve una lista vacía porque no había nada que descargar.
            }

            println "[UNIDAD DE ADQUISICIÓN] Iniciando descarga de activos..."
            pdfLinks.eachWithIndex { link, i ->
                String pdfUrl = link.absUrl("href")
                String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1)
                def targetFile = new File(downloadDir, fileName)
                println " -> Adquiriendo [${i + 1}/${pdfLinks.size()}] ${fileName}"

                byte[] pdfBytes = Jsoup.connect(pdfUrl)
                        .sslSocketFactory(sslContext.getSocketFactory())
                        .ignoreContentType(true).execute().bodyAsBytes()
                targetFile.bytes = pdfBytes

                // Se añaden a la lista.
                def intelPackage = new ScrapedFileInfoDTO(localFile: targetFile, publicUrl: pdfUrl)
                scrapedIntelligence.add(intelPackage)
            }
            println "[UNIDAD DE ADQUISICIÓN] Todos los activos han sido asegurados."
        } catch (HttpStatusException e) {
            // Captura un 404 (página no encontrada). Seguramente festivo o domingo.
            if (e.getStatusCode() == 404) {
                println "[UNIDAD DE ADQUISICIÓN] La URL para la fecha ${targetDate} no existe (404). Día no laborable."
                return Collections.emptyList() // Devuelve una lista vacía.
            } else {
                throw new IOException("Error HTTP inesperado al contactar con el BOE: ${e.getStatusCode()}", e)
            }
        } catch (Exception e) {
            // Cualquier otra excepción (de red, de disco, etc.) se envolvuelve y se lanza hacia arriba
            // para que el GlobalExceptionHandler la convierta en un error 500.
            System.err.println("!! FALLO CRÍTICO en la Unidad de Adquisición:")
            e.printStackTrace()
            throw new RuntimeException("Fallo irrecuperable durante la adquisición de datos para ${targetDate}", e)
        }

        return scrapedIntelligence
    }

    /**
     * Crea y configura un contexto SSL personalizado para Jsoup.
     */
    private SSLContext createSslContext() {
        try {
            def trustStoreFile = getClass().getResourceAsStream("/truststore.p12")
            if (trustStoreFile == null) {
                throw new RuntimeException("No se pudo encontrar 'truststore.p12'.")
            }
            KeyStore trustStore = KeyStore.getInstance("PKCS12")
            trustStore.load(trustStoreFile, "changeit".toCharArray())
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(trustStore)
            SSLContext sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.getTrustManagers(), null)
            return sslContext
        } catch (Exception e) {
            throw new RuntimeException("Fallo crítico durante la configuración del contexto SSL.", e)
        }
    }
}

