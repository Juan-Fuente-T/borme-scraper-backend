package com.lambda.borme_processor.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Excepción personalizada para indicar que un recurso solicitado no ha sido encontrado.
 * La anotación @ResponseStatus le dice a Spring que, por defecto, esta excepción
 * debe resultar en una respuesta HTTP 404 Not Found.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error detallado.
     * @param message El mensaje que describe el recurso que no se encontró.
     */
    ResourceNotFoundException(String message) {
        super(message)
    }
}