package com.example.resource;

import com.example.resource.initializer.KeycloakInitializerConfigurationProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Autowired
    KeycloakInitializerConfigurationProperties keycloakInitializerConfigurationProperties;

    @Bean
    protected Keycloak keyClockAdmin() {

        ResteasyJackson2Provider provider = new ResteasyJackson2Provider();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        provider.setMapper(objectMapper);
        ResteasyProviderFactory providerFactory = new LocalResteasyProviderFactory(new ResteasyProviderFactoryImpl());
        providerFactory.registerProviderInstance(provider);
        RegisterBuiltin.register(providerFactory);

        ResteasyClient resteasyClient = new ResteasyClientBuilderImpl()
                .connectionPoolSize(20)
                .providerFactory(providerFactory)
                .httpEngine(new URLConnectionEngine())
                .build();

        return KeycloakBuilder.builder()
                .grantType(OAuth2Constants.PASSWORD)
                .realm(keycloakInitializerConfigurationProperties.getMasterRealm())
                .clientId(keycloakInitializerConfigurationProperties.getClientId())
                .username(keycloakInitializerConfigurationProperties.getUsername())
                .password(keycloakInitializerConfigurationProperties.getPassword())
                .serverUrl(keycloakInitializerConfigurationProperties.getUrl())
                .resteasyClient(resteasyClient)
                .build();
    }
}
