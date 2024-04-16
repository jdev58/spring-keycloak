package com.example.resource.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SpringSecurityConfig  {

		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

					http.csrf().disable().cors().disable().authorizeHttpRequests()
					.requestMatchers("/user/register/**").permitAll()
					.anyRequest().authenticated()
					.and()
					.oauth2ResourceServer()
					.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()));
			return http.build();
		}


	private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
		JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
		jwtConverter.setJwtGrantedAuthoritiesConverter(new RealmRoleConverter());
		return jwtConverter;
	}
}

