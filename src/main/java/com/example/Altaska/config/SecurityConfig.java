package com.example.Altaska.config;

import com.example.Altaska.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    public SecurityConfig() {
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)authorize.requestMatchers(new String[]{"/", "/login", "/registration", "/confirm", "/error","/css/**", "/js/**", "/icons/**", "/images/**"})).permitAll().anyRequest()).authenticated()).formLogin((form) -> ((FormLoginConfigurer)form.loginPage("/login").defaultSuccessUrl("/account")).permitAll()).logout((logout) -> logout.logoutUrl("/logout").clearAuthentication(true).invalidateHttpSession(true).deleteCookies(new String[]{"JSESSIONID"}).logoutSuccessUrl("/login"));
        return (SecurityFilterChain)http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = (AuthenticationManagerBuilder)http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(this.passwordEncoder());
        return (AuthenticationManager)authenticationManagerBuilder.build();
    }
}
