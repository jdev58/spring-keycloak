package com.example.resource.initializer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "keycloak-initializer")
public class KeycloakInitializerConfigurationProperties {

    @Getter(AccessLevel.NONE)
    private boolean initializeOnStartup;

    public boolean initializeOnStartup() {
        return initializeOnStartup;
    }

    private String masterRealm;

    private String applicationRealm;

    private String clientId;

    private String username;

    private String password;

    private String url;
    private String clientSecret;

}

