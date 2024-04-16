package com.example.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@ConfigurationPropertiesScan("com.example.resource")
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class ResourceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ResourceApplication.class, args);
	}

}
