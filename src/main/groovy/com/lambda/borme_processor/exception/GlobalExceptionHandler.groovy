package com.lambda.borme_processor.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestControllerAdvice
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
     * Captura cualquier error de autenticación (token inválido, sesión expirada, etc.).
     * Genera una respuesta HTTP 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    static ResponseEntity<?> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        def errorDetails = [
                timestamp: new Date(),
                status: HttpStatus.UNAUTHORIZED.value(),
                error: "Unauthorized",
                message: "Error de autenticación: " + ex.getMessage(),
                path: request.getDescription(false).substring(4)
        ]

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED)
    }

    /**
     * Captura errores de acceso denegado (usuario autenticado pero sin permisos).
     */
    @ExceptionHandler(AccessDeniedException.class)
    static ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        def errorDetails = [
                timestamp: new Date(),
                status: HttpStatus.FORBIDDEN.value(),
                error: "Forbidden",
                message: "No tienes permisos para acceder a este recurso.",
                path: request.getDescription(false).substring(4)
        ]

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN)
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
                path: request.getDescription(false).substring(4)
        ]

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}