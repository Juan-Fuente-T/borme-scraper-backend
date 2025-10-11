package com.lambda.borme_processor

import com.lambda.borme_processor.service.BormeParserService
import com.lambda.borme_processor.service.BormeScraperService
import com.lambda.borme_processor.service.PdfParsingService
import com.lambda.borme_processor.service.PersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootApplication
class BormeProcessorApplication{

    static void main(String[] args) {
        SpringApplication.run(BormeProcessorApplication.class, args)
    }
}
