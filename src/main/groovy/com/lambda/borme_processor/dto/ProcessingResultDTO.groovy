package com.lambda.borme_processor.dto

import java.time.LocalDate

class ProcessingResultDTO {
    boolean success
    String message
    LocalDate date
    int filesProcessed
    int companiesFound
    List<String> fileUrls = [] // <-- Incluye lista de URLs
}