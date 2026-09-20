package com.ucop.hethongphanluongbenhvien.config;

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
            // Cho phép xem giao diện tĩnh và các trang thiết kế để test trước
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().permitAll() // Tạm thời permitAll để preview UI
            )
            // Trỏ trang login mặc định về /login của chúng ta
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            )
            // Tạm tắt CSRF để dễ test form (như form đăng xuất) trong giai đoạn UI
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
