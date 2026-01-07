package com.inventory.invflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    //API權限設定
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return  http
                .csrf(csrf -> csrf.disable()) 

                .authorizeHttpRequests(request -> request
                    // 登入、靜態資源
                    .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/login").permitAll()

                    // ===== User 模組：只有 ADMIN =====
                    .requestMatchers("/users/**").hasRole("ADMIN")
                    
                    // ===== Supplier 模組 =====
                    // 新增＆編輯：只有 MANAGER
                    .requestMatchers(HttpMethod.GET, "/suppliers/create").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.POST, "/suppliers/create").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/suppliers/*/edit").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.POST, "/suppliers/*/edit").hasRole("MANAGER")
                    
                    // 狀態切換 (停用/啟用)：ADMIN ＆ MANAGER
                    .requestMatchers(HttpMethod.POST, "/suppliers/*/status").hasAnyRole("ADMIN", "MANAGER")
                
                    // 查詢列表＆詳情：所有登入者
                    .requestMatchers(HttpMethod.GET, "/suppliers/**")
                        .hasAnyRole("ADMIN", "MANAGER", "OPERATOR", "VIEWER")
                    
                    // ===== Item 模組 =====
                    // 新增＆編輯：只有MANAGER ＆ OPERATOR
                    .requestMatchers(HttpMethod.GET, "/items/create").hasAnyRole("MANAGER", "OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/items/create").hasAnyRole("MANAGER", "OPERATOR")
                    .requestMatchers(HttpMethod.GET, "/items/*/edit").hasAnyRole("MANAGER", "OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/items/*/edit").hasAnyRole("MANAGER", "OPERATOR")

                    // 狀態切換 (停用/啟用)：ADMIN ＆ MANAGER
                    .requestMatchers(HttpMethod.POST, "/items/*/status").hasAnyRole("ADMIN", "MANAGER")

                    // 查詢列表＆詳情：所有登入者
                    .requestMatchers(HttpMethod.GET, "/items/**")
                        .hasAnyRole("ADMIN", "MANAGER", "OPERATOR", "VIEWER")

                    // ===== InventoryLog 模組 =====
                    // 新增＆編輯：只有MANAGER / OPERATOR
                    .requestMatchers(HttpMethod.GET, "/inventorylogs/create").hasAnyRole("MANAGER", "OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/inventorylogs/create").hasAnyRole("MANAGER", "OPERATOR")
                    .requestMatchers(HttpMethod.GET, "/inventorylogs/adjust").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.POST, "/inventorylogs/adjust").hasRole("MANAGER")

                    // 查詢列表＆詳情：所有登入者
                    .requestMatchers(HttpMethod.GET, "/inventorylogs/**")
                        .hasAnyRole("ADMIN", "MANAGER", "OPERATOR", "VIEWER")

                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
                )
                .build();
    } 

    //密碼加密
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}