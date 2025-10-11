package com.lambda.borme_processor.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service

@Service
class PdfParsingService {

    /**
     * Extrae el texto completo del PDF preservando saltos de línea.
     * No aplana el texto (no sustituye \n por espacios) para mantener la estructura lógica del documento.
     * @param pdfFile Fichero PDF desde el cual extraer el texto.
     * @return Texto completo extraído, con saltos de línea preservados.
     */
    String extractTextFromFile(File pdfFile) {
        println "[UNIDAD DE ANÁLISIS] Procesando activo: ${pdfFile.name}"
        PDDocument document = null
        try {
            // Carga el PDF
            document = Loader.loadPDF(pdfFile)

            // Extrae el texto tal cual, sin modificar saltos de línea
            PDFTextStripper stripper = new PDFTextStripper()
            String rawText = stripper.getText(document)

            // Limpieza ligera: elimina espacios en blanco redundantes, pero conserva saltos de línea
            return rawText
                    .replaceAll(/[ \t]+/, " ")      // compacta múltiples espacios en uno
                    .replaceAll(/\s+\n/, "\n")      // elimina espacios antes de un salto de línea
                    .replaceAll(/\n{3,}/, "\n\n")   // evita acumulaciones excesivas de líneas en blanco
                    .trim()
        } finally {
            // Asegura que el documento se cierre siempre para liberar recursos,
            // incluso si ocurre un error durante la extracción.
            if (document != null) {
                document.close()
            }
        }
    }
}