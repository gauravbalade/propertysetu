package com.gaurav.property.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gaurav.property.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${FRONTEND_ORIGIN:http://localhost:5173}")
    private String frontendOrigin;

    @Value("${FRONTEND_ORIGINS:}")
    private String additionalFrontendOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>(List.of(
                frontendOrigin,
                "https://frontend-six-amber-63.vercel.app",
                "http://localhost:5173"
        ));

        if (additionalFrontendOrigins != null && !additionalFrontendOrigins.isBlank()) {
            for (String origin : additionalFrontendOrigins.split(",")) {
                String normalized = origin.trim();
                if (!normalized.isBlank() && !allowedOrigins.contains(normalized)) {
                    allowedOrigins.add(normalized);
                }
            }
        }

        // Vercel creates multiple deployment aliases. Allow the project's Vercel
        // origins without having to hard-code every preview URL.
        configuration.setAllowedOriginPatterns(List.of(
                "https://*.vercel.app",
                "http://localhost:[*]",
                frontendOrigin
        ));
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter(
            JwtService jwtService) {

        return new OncePerRequestFilter() {

            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain)
                    throws ServletException, IOException {

                String header = request.getHeader("Authorization");

                if (header != null && header.startsWith("Bearer ")) {
                    try {
                        String token = header.substring(7);

                        var claims = jwtService.parseToken(token);
                        String username = claims.getSubject();
                        String role = claims.get("role", String.class);

                        var authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + role));

                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        authorities);

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);

                    } catch (Exception ignored) {
                        SecurityContextHolder.clearContext();
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OncePerRequestFilter jwtAuthenticationFilter)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()))

            .sessionManagement(session -> session
                    .sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login",
                            "/api/auth/verify-msg91-token",
                            "/api/auth/demo-otp/**",
                            "/api/auth/verify-otp",
                            "/api/auth/resend-otp",
                            "/api/auth/forgot-password",
                            "/api/auth/reset-password",
                            "/api/payments/webhook/razorpay",
                            "/api/health",
                            "/",
                            "/error")
                    .permitAll()

                    .requestMatchers(
                            "/api/admin/**")
                    .hasRole("ADMIN")

                    .requestMatchers(
                            "/api/applications",
                            "/api/applications/**",
                            "/api/payments/**",
                            "/api/owners/**",
                            "/api/properties/**",
                            "/api/locations/**",
                            "/api/documents/**")
                    .hasAnyRole(
                            "APPLICANT",
                            "OFFICER",
                            "ADMIN")

                    .requestMatchers(
                            "/api/verifications/**",
                            "/api/audit/**")
                    .hasAnyRole(
                            "OFFICER",
                            "ADMIN")

                    .anyRequest()
                    .authenticated())

            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}