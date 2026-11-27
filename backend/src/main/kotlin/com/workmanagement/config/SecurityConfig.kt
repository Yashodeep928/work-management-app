package com.workmanagement.backend.config

import com.workmanagement.backend.user.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun userDetailsService(users: UserRepository): UserDetailsService = UserDetailsService { email ->
        val user = users.findByEmail(email.trim().lowercase())
            ?: throw UsernameNotFoundException("User not found")
        User.withUsername(user.email).password(user.passwordHash).roles("USER").build()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .anyRequest().authenticated()
            }
            .httpBasic { }
        return http.build()
    }
}
