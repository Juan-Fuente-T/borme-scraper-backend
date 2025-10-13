package com.lambda.borme_processor.utils


import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class DateUtils {
    public static LocalDate parseDate(String dateStr) {
        if (!dateStr) return null

        try {
            // DateTimeFormatterBuilder con pivote inteligente
            def formatter = new DateTimeFormatterBuilder()
                    .appendPattern("dd.MM.")
                    .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)  // Pivote en año 2000
                    .toFormatter()

            return LocalDate.parse(dateStr, formatter)
        } catch (Exception e) {
            println "[PARSER] Error parseando fecha: $dateStr - ${e.message}"
            return null
        }
    }
}
