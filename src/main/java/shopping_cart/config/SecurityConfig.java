package shopping_cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(withDefaults())
        .csrf(AbstractHttpConfigurer::disable) // Required to allow POST/PUT/DELETE from Swagger
        .authorizeHttpRequests(
            auth ->
                auth
                    // 1. Let everyone see the Swagger UI
                    .requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**")
                    .permitAll()

                    // 2. Let everyone access Login and Registration
                    .requestMatchers("/api/user/login", "/api/user/register", "/api/user/**")
                    .permitAll()
                    .requestMatchers("/api/user/**")
                    .permitAll()
                    .requestMatchers("/api/basket/**")
                    .permitAll()
                    .requestMatchers("/api/history/**")
                    .permitAll()
                    .requestMatchers("/api/products/**")
                    .permitAll()
                    .requestMatchers("/api/kaufland/**")
                    .permitAll()

                    // Като допълнителна застраховка, разрешаваме всичко под /api/**
                    .requestMatchers("/api/**")
                    .permitAll()
                    // 3. For the demo, permit all other /api/** calls
                    // OR change .authenticated() to .permitAll() temporarily to test
                    .requestMatchers("/api/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setExposedHeaders(List.of("Session-Id"));
    configuration.setAllowedHeaders(
        List.of(
            "Authorization", "Content-Type", "Session-Id", "X-Requested-With", "Accept", "Origin"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
