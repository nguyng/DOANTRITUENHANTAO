package com.ucop.hethongphanluongbenhvien.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/public/**", "/hms/**", "/display", "/display/**").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                
                // Phân quyền theo Role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/nurse/**").hasAnyRole("NURSE", "ADMIN", "DOCTOR")
                
                // Cho phép public verify (lễ tân không cần đăng nhập)
                .requestMatchers("/api/bhyt/verify").permitAll()
                
                // Các trang bệnh nhân (cần đăng nhập)
                .requestMatchers("/chat/**", "/ticket/**", "/tickets/**", "/profile/**",
                                 "/visits/**", "/directions/**", "/change-password",
                                 "/bhyt/**", "/api/bhyt/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Tạm thời tắt CSRF cho dễ gọi API (vd: /chat/send), trong thực tế nên bật và truyền token
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Chuyển hướng người dùng sau khi đăng nhập thành công tùy theo Role.
     */
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/chat"; // Mặc định cho PATIENT

            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    redirectUrl = "/admin/dashboard";
                    break;
                } else if ("ROLE_NURSE".equals(role) || "ROLE_DOCTOR".equals(role)) {
                    redirectUrl = "/nurse/queue";
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
