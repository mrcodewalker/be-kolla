package com.example.kolla.config;

import com.example.kolla.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints - change-password requires authentication
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/change-password").authenticated()
                // Public endpoints
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/roles",
                    "/api/v1/attendance-logs/create-with-token",
                        "/api/v1/attendance-logs/{id}/leave-with-token",

                    "/api/v1/messages/create",
                    "/ws/**",
                    "/static/**",
                    "/*.html",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/webjars/**"
                ).permitAll()
                
                // Admin only endpoints - Quản trị hệ thống
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Quản lý phòng ban
                .requestMatchers(HttpMethod.POST, "/api/v1/departments/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.PUT, "/api/v1/departments/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/departments/**").hasAnyRole("ADMIN", "SECRETARY")
                // Quản lý roles
                .requestMatchers(HttpMethod.POST, "/api/v1/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/roles/**").hasRole("ADMIN")
                // Quản lý phòng họp
                .requestMatchers(HttpMethod.POST, "/api/v1/rooms/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.PUT, "/api/v1/rooms/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/rooms/**").hasAnyRole("ADMIN", "SECRETARY")
                // Quản lý người dùng
                .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                
                // Secretary endpoints - Thư ký cuộc họp
                // Quản lý tài liệu
                .requestMatchers(HttpMethod.POST, "/api/v1/documents/**").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/documents/**").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/documents/**").hasAnyRole("SECRETARY", "ADMIN")
                // Quản lý lịch sử chỉnh sửa tài liệu
                .requestMatchers(HttpMethod.POST, "/api/v1/documents/*/history").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/documents/history/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/documents/history/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/documents/*/history/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/documents/history/**").authenticated()
                // Quản lý biên bản
                .requestMatchers("/api/v1/meetings/*/minutes/**").hasAnyRole("SECRETARY", "ADMIN")
                // Quản lý ghi chú cuộc họp
                .requestMatchers(HttpMethod.POST, "/api/v1/meeting-notes/**").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/meeting-notes/**").hasAnyRole("SECRETARY", "ADMIN")
                
                // Meeting management - Quản lý cuộc họp
                .requestMatchers(HttpMethod.PUT, "/api/v1/meetings/*/is-meeting").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/meetings/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.PUT, "/api/v1/meetings/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/meetings/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.GET, "/api/v1/meetings/**").authenticated()

                // Recording management - Quản lý bản ghi
                .requestMatchers(HttpMethod.POST, "/api/v1/recordings/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/recordings/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/recordings/**").authenticated()

                // File management - Quản lý file
                .requestMatchers(HttpMethod.POST, "/api/v1/files/upload").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.POST, "/api/v1/files/bucket/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/files/**").hasAnyRole("ADMIN", "SECRETARY")
                .requestMatchers(HttpMethod.GET, "/api/v1/files/**").authenticated()

                // Member management - Quản lý thành viên phòng
                    .requestMatchers(HttpMethod.POST, "/api/v1/members/check-meeting-access").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/members/approve").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/members/reject/**").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/members/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/members/**").hasAnyRole("SECRETARY", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/members/request").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/**").authenticated()

                // User accessible endpoints - Người dùng thông thường
                // Messages
                .requestMatchers("/api/v1/messages/**").authenticated()
                // Notifications
                .requestMatchers("/api/v1/notifications/**").authenticated()
                // Attendance
                .requestMatchers(HttpMethod.POST, "/api/v1/attendance-logs/create-with-token").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/attendance-logs/*/leave-with-token").permitAll()
                .requestMatchers("/api/v1/attendance-logs/**").authenticated()
                // Meeting tags
                .requestMatchers(HttpMethod.GET, "/api/v1/meeting-tags/**").authenticated()
                // View only endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/departments/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/roles/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/rooms/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").authenticated()
                
                // Default policy
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",  // Angular dev server
            "http://localhost:3000",   // React dev server
            "http://localhost:8080",   // Vue dev server
            "https://localhost:8080",   // Vue dev server
            "http://localhost:8888",   // Local dev
            "https://kolla.click",     // Production
            "https://signal.kolla.click",
            "https://meeting.kolla.click",
            "https://meet.kolla.click",
            "https://kma.kolla.click",
            "https://36.50.54.109",
            "https://36.50.54.109:8081"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache CORS config trong 1 giờ
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}