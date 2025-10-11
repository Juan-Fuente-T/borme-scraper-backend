// package com.lambda.borme_processor
package com.lambda.borme_processor.service

import com.lambda.borme_processor.dto.ScrapedFileInfoDTO
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
		// La unidad gestiona su propio directorio temporal, específico para la fecha.
		def downloadDir = new File("temp_borme_${targetDate}")

		// Se crea la lista que contendrá los paquetes de inteligencia completos.
		List<ScrapedFileInfoDTO> scrapedIntelligence = []

		// Construye la URL dinámicamente a partir de la fecha.
		String bormeUrl = String.format(BORME_URL_TEMPLATE, targetDate.getYear(), targetDate.getMonthValue(), targetDate.getDayOfMonth())

		if (downloadDir.exists() && downloadDir.listFiles().length > 0) {
			println "[UNIDAD DE ADQUISICIÓN] Activos para esta fecha ya presentes en caché local. Misión de adquisición cancelada."
			// Opcional: limpiar directorio si se desea forzar la redescarga siempre.
			// downloadDir.deleteDir()
			return downloadDir.listFiles().collect { file ->
				new ScrapedFileInfoDTO(
						localFile: file,
						publicUrl: bormeUrl + "pdfs/" + file.name
				)
			}
		}

		try {
			SSLContext sslContext = createSslContext()

			println "[UNIDAD DE ADQUISICIÓN] Extrayendo lista de objetivos de ${bormeUrl}..."
			Document doc = Jsoup.connect(bormeUrl).sslSocketFactory(sslContext.getSocketFactory()).get()
			//def pdfLinks = doc.select('a[href$=.pdf]')
			def pdfLinks = doc.select('a[href*="BORME-A-"]:not([href$="-99.pdf"])')
			println "[UNIDAD DE ADQUISICIÓN] Se han identificado ${pdfLinks.size()} objetivos."


			if (!downloadDir.exists()) {
				downloadDir.mkdirs()
			}

			println "[UNIDAD DE ADQUISICIÓN] Iniciando descarga de activos..."
			pdfLinks.eachWithIndex { link, i ->
				String pdfUrl = link.absUrl("href")
				String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1)
				def targetFile = new File(downloadDir, fileName)
				println " -> Adquiriendo [${i + 1}/${pdfLinks.size()}] ${fileName}"

				// Solo se descarga si el fichero no existe localmente.
				if (!targetFile.exists()) {
					byte[] pdfBytes = Jsoup.connect(pdfUrl)
							.sslSocketFactory(sslContext.getSocketFactory())
							.ignoreContentType(true).execute().bodyAsBytes()
					targetFile.bytes = pdfBytes
				}
				// Se añaden a la lista.
				def intelPackage = new ScrapedFileInfoDTO(localFile: targetFile, publicUrl: pdfUrl)
				scrapedIntelligence.add(intelPackage)
			}
			println "[UNIDAD DE ADQUISICIÓN] Todos los activos han sido asegurados."
		} catch (Exception e) {
			System.err.println("!! FALLO CRÍTICO en la Unidad de Adquisición:")
			e.printStackTrace()
		}

		return scrapedIntelligence
	}

	/**
	 * Crea y configura un contexto SSL personalizado para Jsoup.
	 */
	private SSLContext createSslContext() {
		try {
			def trustStoreFile = getClass().getResourceAsStream("/truststore.p12")
			if (trustStoreFile == null) { throw new RuntimeException("No se pudo encontrar 'truststore.p12'.") }
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

