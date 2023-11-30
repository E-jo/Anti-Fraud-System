package antifraud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> {
                    csrf.disable();
                    csrf.ignoringRequestMatchers("/h2-console/**");
                })
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .exceptionHandling(handling -> handling
                        .accessDeniedHandler(getAccessDeniedHandler())
                )
                .headers(headers -> headers.frameOptions().disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/auth/user", "/actuator/shutdown").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip", "/api/antifraud/stolencard").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/{ip}", "/api/antifraud/stolencard").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip", "/api/antifraud/stolencard").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access", "/api/auth/access/").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role", "/api/auth/role/").hasRole("ADMINISTRATOR")
                        .anyRequest().fullyAuthenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    @Bean
    public AccessDeniedHandler getAccessDeniedHandler() {
        ObjectMapper objectMapper = new ObjectMapper();

        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", LocalDateTime.now().toString());
            data.put("status", HttpStatus.FORBIDDEN.value());
            data.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
            data.put("message", "Access Denied!");
            data.put("path", request.getRequestURI());

            response.getWriter().println(objectMapper.writeValueAsString(data));
        };
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(13);
    }

}
