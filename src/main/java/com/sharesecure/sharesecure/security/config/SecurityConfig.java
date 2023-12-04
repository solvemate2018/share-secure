package com.sharesecure.sharesecure.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sharesecure.sharesecure.security.jwt.AuthEntryPointJwt;
import com.sharesecure.sharesecure.security.jwt.AuthTokenFilter;
import com.sharesecure.sharesecure.security.services.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authTokenFilter(){
        return new AuthTokenFilter();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http
    , BCryptPasswordEncoder bCryptPasswordEncoder
    , UserDetailsServiceImpl userDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(authorizeRequests -> 
        authorizeRequests.requestMatchers(HttpMethod.POST).permitAll()
        .requestMatchers(HttpMethod.GET).permitAll());
        http.exceptionHandling(auth -> auth.authenticationEntryPoint(unauthorizedHandler));  // Set custom authentication entry point
        // http.csrf((csrf) -> csrf
        //     .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		// 	);
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.disable());

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
