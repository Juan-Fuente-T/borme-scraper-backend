package com.lambda.borme_processor.controller

import com.lambda.borme_processor.dto.LoginResponseDTO
import com.lambda.borme_processor.dto.UserInfoDTO
import com.lambda.borme_processor.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para manejar las operaciones relacionadas con la autenticación.
 * Proporciona endpoints para el inicio de sesión y la obtención de información del usuario autenticado.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
class AuthController {

    @Autowired
    private AuthService authService

    /**
     * Endpoint para manejar el inicio de sesión.
     * Este método delega la lógica de autenticación al servicio de autenticación.
     *
     * POST /api/auth/login
     *
     * @param authentication El objeto `Authentication` proporcionado automáticamente por Spring Security,
     *                       que contiene las credenciales del usuario y el estado de autenticación.
     * @return Un `ResponseEntity` que contiene un `LoginResponseDTO` con el resultado del inicio de sesión.
     */
    @PostMapping("/login")
    ResponseEntity<LoginResponseDTO> login(Authentication authentication) {
        return authService.handleLogin(authentication)
    }

    /**
     * Endpoint para obtener la información del usuario autenticado actual.
     * Este método delega la lógica al servicio de autenticación.
     *
     * GET /api/auth/me
     *
     * @param authentication El objeto `Authentication` proporcionado automáticamente por Spring Security,
     *                       que contiene las credenciales del usuario y el estado de autenticación.
     * @return Un `ResponseEntity` que contiene un `UserInfoDTO` con la información del usuario autenticado.
     */
    @GetMapping("/me")
    ResponseEntity<UserInfoDTO> getCurrentUser(Authentication authentication) {
        return authService.getCurrentUser(authentication)
    }
}