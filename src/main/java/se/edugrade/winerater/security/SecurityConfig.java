package se.edugrade.winerater.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";
    private static final String ALL = "/**";
    private static final String WINES = "/wines";
    private static final String RATINGS = "/ratings";
    private static final String USERS = "/users";
    private static final String API = "/api";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(java.util.Arrays.asList("http://localhost:5173", "http://localhost:3000"));
                    config.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(java.util.Arrays.asList("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth

                        // 🌐 Public endpoints
                        .requestMatchers("/auth" + ALL).permitAll()
                        .requestMatchers("/alive").permitAll()
                        .requestMatchers("/swagger-ui" + ALL, "/api-docs" + ALL, "/v3/api-docs" + ALL).permitAll()
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console" + ALL).permitAll()

                        // 🍷 Wines - GET är publikt
                        .requestMatchers(HttpMethod.GET, API + WINES + ALL).permitAll()

                        // ⭐ Ratings - GET är publikt
                        .requestMatchers(HttpMethod.GET, API + RATINGS + ALL).permitAll()

                        // 🔒 ADMIN-only actions
                        .requestMatchers(HttpMethod.PUT, API + WINES + ALL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API + WINES + ALL).hasRole(ADMIN)

                        // ⭐ Ratings + Wine creation (USER + ADMIN)
                        .requestMatchers(HttpMethod.POST, API + RATINGS + ALL).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, API + WINES + ALL).hasAnyRole(USER, ADMIN)

                        // 👤 Users (admin kan ev. hantera users)
                        .requestMatchers(API + USERS + ALL).authenticated()

                        // 🔐 everything else
                        .anyRequest().authenticated()
                )

                // ⚠️ Custom error handling (kan bytas mot din GlobalExceptionHandler senare)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Not authorized\"}");
                        })
                        .accessDeniedHandler((req, res, accessDeniedEx) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Forbidden action\"}");
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // för H2 console (om du använder den)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
