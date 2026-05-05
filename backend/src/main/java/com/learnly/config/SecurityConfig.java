package com.learnly.config;

import com.learnly.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtFilter,
                          @Value("${learnly.cors.allowed-origins}") String allowedOriginsCsv) {
        this.jwtFilter = jwtFilter;
        this.allowedOrigins = List.of(allowedOriginsCsv.split(","));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(c -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/courses", "/api/courses/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/lessons/*/comments").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/courses/*/reviews").permitAll()
                .requestMatchers("/actuator/**", "/error").permitAll()
                // Admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else requires auth
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        var src = new UrlBasedCorsConfigurationSource();
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Accept-Language"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }
}
