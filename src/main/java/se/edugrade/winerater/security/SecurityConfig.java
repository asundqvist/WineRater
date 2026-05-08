package se.edugrade.winerater.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${app.security.h2-console-enabled:false}")
    private boolean h2ConsoleEnabled;

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";
    private static final String ALL = "/**";
    private static final String WINES = "/wines";
    private static final String RATINGS = "/ratings";
    private static final String WEATHER = "/weather";
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth" + ALL).permitAll()
                        .requestMatchers("/alive").permitAll()
                        .requestMatchers("/swagger-ui" + ALL, "/api-docs" + ALL, "/v3/api-docs" + ALL).permitAll()
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console" + ALL).access((authentication, context) ->
                                new AuthorizationDecision(h2ConsoleEnabled))
                        .requestMatchers(HttpMethod.GET, API + WINES + ALL).permitAll()
                        .requestMatchers(HttpMethod.GET, API + RATINGS + ALL).permitAll()
                        .requestMatchers(HttpMethod.GET, API + WEATHER + ALL).permitAll()
                        .requestMatchers(HttpMethod.PUT, API + WINES + ALL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API + WINES + ALL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, API + RATINGS + ALL).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, API + WINES + ALL).hasAnyRole(USER, ADMIN)
                        .anyRequest().authenticated()
                )
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
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
