package com.example.kolla.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${kolla.openapi.dev-url}")
    private String devUrl;

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server()
                .url(devUrl)
                .description("Server URL in Development environment");

        Server prodServer = new Server()
                .url("https://signal.kolla.click")
                .description("Server URL in Production environment");

        Contact contact = new Contact()
                .email("hai@actvn.edu.vn")
                .name("Kolla")
                .url("https://kolla.click");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Kolla API Management")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for Kolla application.")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(java.util.List.of(devServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT auth description")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}