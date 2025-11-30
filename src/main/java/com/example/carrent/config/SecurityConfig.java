package com.example.carrent.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения.
 *
 * Этот класс отвечает за:
 * - Настройку аутентификации и авторизации пользователей
 * - Определение правил доступа к различным URL приложения
 * - Конфигурацию шифрования паролей
 * - Настройку CSRF защиты
 * - Определение страниц входа/выхода
 *
 * @author Система аренды автомобилей
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Включает поддержку аннотаций @PreAuthorize, @PostAuthorize и т.д.
public class SecurityConfig {

    /**
     * Сервис для загрузки данных пользователя из БД.
     * Используется Spring Security для проверки учетных данных при входе.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param userDetailsService сервис загрузки пользователей
     */
    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Bean для кодирования паролей.
     *
     * Использует алгоритм BCrypt для безопасного хеширования паролей.
     * BCrypt автоматически добавляет соль и использует адаптивную функцию хеширования,
     * что делает практически невозможным восстановление исходного пароля.
     *
     * @return экземпляр BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Провайдер аутентификации на основе DAO (Data Access Object).
     *
     * Связывает UserDetailsService (для загрузки пользователя из БД)
     * с PasswordEncoder (для проверки пароля).
     *
     * Процесс аутентификации:
     * 1. Пользователь вводит username и password
     * 2. UserDetailsService загружает пользователя из БД по username
     * 3. PasswordEncoder сравнивает введенный пароль с хешем из БД
     * 4. При совпадении аутентификация успешна
     *
     * @return настроенный провайдер аутентификации
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    /**
     * Главная цепочка фильтров безопасности.
     *
     * Определяет правила авторизации для всех URL приложения.
     * Порядок правил важен - проверка идет сверху вниз, первое совпадение применяется.
     *
     * @param http объект конфигурации HTTP безопасности
     * @return настроенную цепочку фильтров
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ========== ПРАВИЛА АВТОРИЗАЦИИ ==========
                .authorizeHttpRequests(auth -> auth

                        // --- ПУБЛИЧНЫЕ СТРАНИЦЫ (доступны всем без авторизации) ---

                        // Главная страница - доступна всем
                        .requestMatchers(HttpMethod.GET, "/").permitAll()

                        // Страницы входа и регистрации - доступны всем
                        .requestMatchers("/login", "/register").permitAll()

                        // Информационные страницы - доступны всем
                        .requestMatchers("/about").permitAll()

                        // --- КАТАЛОГ АВТОМОБИЛЕЙ (публичный доступ) ---

                        // GET /cars - основной каталог доступен всем
                        .requestMatchers(HttpMethod.GET, "/cars").permitAll()

                        // Детали машины доступны всем
                        .requestMatchers(HttpMethod.GET, "/cars/*").permitAll()

                        // --- СПЕЦИАЛЬНЫЕ КАТАЛОГИ (только для сотрудников) ---

                        // Каталог TAXI - только для сотрудников, менеджеров и админов
                        .requestMatchers("/cars/taxi").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                        // Каталог DELIVERY - только для сотрудников, менеджеров и админов
                        .requestMatchers("/cars/delivery").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                        // --- REST API ENDPOINTS ---

                        // GET /api/cars - доступен всем (для внешних клиентов)
                        .requestMatchers(HttpMethod.GET, "/api/cars", "/api/cars/*").permitAll()

                        // POST, PUT /api/cars - только менеджеры и админы
                        .requestMatchers(HttpMethod.POST, "/api/cars").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cars/*").hasAnyRole("MANAGER", "ADMIN")

                        // DELETE /api/cars/{id} - только админы
                        .requestMatchers(HttpMethod.DELETE, "/api/cars/*").hasRole("ADMIN")

                        // PATCH /api/cars/{id}/availability - менеджеры и админы
                        .requestMatchers(HttpMethod.PATCH, "/api/cars/*/availability")
                        .hasAnyRole("MANAGER", "ADMIN")

                        // --- СТАТИЧЕСКИЕ РЕСУРСЫ (CSS, JS, изображения) ---

                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // --- ТРЕБУЮТ АВТОРИЗАЦИИ ---

                        // Личный кабинет - только для авторизованных
                        .requestMatchers("/account/**").authenticated()

                        // Бронирование - только для авторизованных
                        .requestMatchers("/booking", "/booking/**").authenticated()

                        // --- АДМИНИСТРАТИВНЫЕ РАЗДЕЛЫ ---

                        // Админ-панель - только для администраторов
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Менеджерская панель - для менеджеров и админов
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")

                        // --- ВСЕ ОСТАЛЬНЫЕ ЗАПРОСЫ ---

                        // Все остальные страницы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // ========== CSRF ЗАЩИТА ==========
                .csrf(csrf -> csrf
                        // Отключаем CSRF для REST API
                        // Причина: REST API обычно используется мобильными приложениями
                        // и SPA, которым сложно работать с CSRF токенами
                        .ignoringRequestMatchers("/api/**")
                )

                // ========== ФОРМА ВХОДА ==========
                .formLogin(form -> form
                        // URL страницы входа
                        .loginPage("/login")

                        // Разрешаем всем доступ к странице входа
                        .permitAll()

                        // После успешного входа перенаправляем в личный кабинет
                        .defaultSuccessUrl("/account", true)

                        // При ошибке входа возвращаем на /login?error
                        .failureUrl("/login?error")
                )

                // ========== ВЫХОД ==========
                .logout(logout -> logout
                        // URL для выхода
                        .logoutUrl("/logout")

                        // После выхода перенаправляем на главную с параметром
                        .logoutSuccessUrl("/?logout")

                        // Разрешаем всем доступ к функции выхода
                        .permitAll()

                        // Удаляем сессию при выходе
                        .invalidateHttpSession(true)

                        // Удаляем куки
                        .deleteCookies("JSESSIONID")
                )

                // ========== ДОПОЛНИТЕЛЬНЫЕ НАСТРОЙКИ ==========

                // Разрешаем отображение страниц в iframe с того же домена
                // (нужно для некоторых административных панелей)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}
