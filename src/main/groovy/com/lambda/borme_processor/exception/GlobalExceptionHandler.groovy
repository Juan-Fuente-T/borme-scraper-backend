package com.lambda.borme_processor.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * Captura cualquier excepción genérica que no haya sido manejada
     * y la transforma en una respuesta HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    static ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        def errorDetails = [
                timestamp: new Date(),
                status: HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error: "Internal Server Error",
                message: "Ha ocurrido un error inesperado en el servidor: " + ex.getMessage(),
                path: request.getDescription(false).substring(4) // Extrae la URL
        ]

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}