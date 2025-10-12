package com.lambda.borme_processor.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import com.lambda.borme_processor.exception.ResourceNotFoundException


@ControllerAdvice
class GlobalExceptionHandler {
    /**
     * Se activa cuando un servicio lanza una ResourceNotFoundException.
     * Devuelve un código HTTP 404 claro y específico.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    static ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        def errorDetails = [
                timestamp: new Date(),
                status: HttpStatus.NOT_FOUND.value(),
                error: "Not Found",
                message: ex.getMessage(),
                path: request.getDescription(false).substring(4)
        ]
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND)
    }

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