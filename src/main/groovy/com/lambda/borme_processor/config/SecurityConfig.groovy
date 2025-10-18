package com.lambda.borme_processor.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()  // Desactivar CSRF para API REST
                .authorizeHttpRequests { auth ->
                    auth
                    // Endpoints públicos (sin autenticación)
                            .requestMatchers(HttpMethod.GET, "/api/borme/health").permitAll()

                    // Todos los demás endpoints requieren autenticación
                            .requestMatchers("/api/borme/**").authenticated()

                            .anyRequest().permitAll()
                }
                .httpBasic()  // Autenticación HTTP Basic
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Sin sesiones

        return http.build()
    }

    @Bean
    UserDetailsService userDetailsService() {
        def user = User.builder()
                .username("processor")
                .password(passwordEncoder().encode("borme-processor"))
                .roles("USER")
                .build()

        return new InMemoryUserDetailsManager(user)
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder()
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration()
        //configuration.setAllowedOrigins(["http://localhost:5173"])  // Frontend Svelte
        configuration.setAllowedOrigins(["*"])  // Temporalmente permisivo
        configuration.setAllowedMethods(["GET", "POST", "PUT", "DELETE", "OPTIONS"])
        configuration.setAllowedHeaders(["*"])
        configuration.setAllowCredentials(true)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}