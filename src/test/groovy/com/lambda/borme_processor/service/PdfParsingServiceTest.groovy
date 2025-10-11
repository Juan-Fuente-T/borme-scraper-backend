package com.lambda.borme_processor.service

import org.junit.jupiter.api.Test
import java.io.File

class PdfParsingServiceTest {

    @Test
    void testExtractTextFromLocalPdf() throws Exception {
        PdfParsingService parsingService = new PdfParsingService()

        // Ruta del PDF que mencionaste
        File pdfFile = new File("C:\\Users\\Juan\\Desktop\\Mis_proyectos\\Java\\PruebasTecnicas\\Lambda Telematics\\borme-processor\\borme_pdfs\\BORME-A-2025-165-03.pdf")

        String text = parsingService.extractTextFromFile(pdfFile)

        System.out.println("---- INICIO DEL TEXTO ----")
        System.out.println(text.substring(0, Math.min(3000, text.length())))
        System.out.println("---- FIN DEL TEXTO ----")
    }
}