package sa.store.retaildiscount.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/h2-console/**",
                    "/error",
                    "/health",
                    "/info",
                    "/metrics",
                    "/prometheus",
                    "/favicon.ico",
                    "/static/**",
                    "/public/**",
                    "/resources/**",
                    "/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}