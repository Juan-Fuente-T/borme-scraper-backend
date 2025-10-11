package com.lambda.borme_processor.service

import org.junit.jupiter.api.Test
import java.io.File

class BormeParserServiceTest {

    @Test
    void testParseCompaniesFromPdfText() throws Exception {
        PdfParsingService parsingService = new PdfParsingService()
        BormeParserService parserService = new BormeParserService()

        File pdfFile = new File("C:\\Users\\Juan\\Desktop\\Mis_proyectos\\Java\\PruebasTecnicas\\Lambda Telematics\\borme-processor\\borme_pdfs\\BORME-A-2025-165-03.pdf")

        String text = parsingService.extractTextFromFile(pdfFile)
        var companies = parserService.extractCompaniesFromText(text, pdfFile.getPath())

        companies.forEach(c -> {
            System.out.println("----")
            System.out.println("Nombre: " + c.getName())
            System.out.println("Objeto: " + c.getObject())
            System.out.println("Capital: " + c.getCapital())
        })
    }
}
