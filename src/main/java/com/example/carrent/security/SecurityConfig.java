package com.example.carrent.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // статика
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/css/**","/js/**","/images/**","/webjars/**").permitAll()

                        // публичные страницы
                        .requestMatchers("/", "/index", "/about", "/cars",
                                "/login", "/register", "/error", "/error/**").permitAll()

                        // каталоги такси/доставки — только для EMPLOYEE, MANAGER, ADMIN
                        .requestMatchers("/carstaxi", "/cars/delivery",
                                "/cars-taxi", "/cars-delivery")
                        .hasAnyRole("EMPLOYEE","MANAGER","ADMIN")

                        // кабинет и бронирования
                        .requestMatchers("/account/**").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/booking").authenticated()
                        .requestMatchers(HttpMethod.POST, "/booking").authenticated()

                        // менеджер и админ
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER","ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().denyAll()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/account", false)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(Customizer.withDefaults())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
