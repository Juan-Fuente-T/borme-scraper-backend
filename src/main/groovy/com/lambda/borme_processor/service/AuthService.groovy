package com.lambda.borme_processor.service

import com.lambda.borme_processor.dto.LoginResponseDTO
import com.lambda.borme_processor.dto.UserInfoDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service

/**
 * Servicio responsable de manejar las operaciones relacionadas con la autenticación.
 */
@Service
class AuthService {

    /**
     * Maneja el proceso de inicio de sesión validando el objeto de autenticación.
     * La seguridad es manejada por la cadena de filtros de Spring Security
     *
     * @param authentication El objeto `Authentication`, inyectado automáticamente por Spring Security
     * @return Un `ResponseEntity` que contiene un `LoginResponseDTO` con el resultado del inicio de sesión.
     * @throws BadCredentialsException si el objeto de autenticación es nulo o el usuario no está autenticado desde Spring Security.
     */
    static ResponseEntity<LoginResponseDTO> handleLogin(Authentication authentication) {
        def response = new LoginResponseDTO(
                success: true,
                message: "Login exitoso",
                username: authentication.name
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Recupera la información del usuario autenticado actual.
     * La seguridad es manejada por la cadena de filtros de Spring Security
     *
     * @param authentication El objeto `Authentication`, inyectado automáticamente por Spring Security
     * solo si el usuario ha pasado la autenticación.
     * @return Un `ResponseEntity` que contiene un `UserInfoDTO` con la información del usuario.
     * @throws AuthenticationException (o una subclase como BadCredentialsException) desde Spring Security.
     */
    static ResponseEntity<UserInfoDTO> getCurrentUser(Authentication authentication) {
        def response = new UserInfoDTO(
                    success: true,
                    username: authentication.name,
                    roles: authentication.authorities.collect { it.authority },
                    message: null
            )
            return ResponseEntity.ok(response)
        }
}