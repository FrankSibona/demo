package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata general que aparece en Swagger UI (título, descripción, versión).
 * La documentación de cada endpoint puntual va con @Operation en el
 * controller correspondiente.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI demoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TP3 · Autenticación JWT y Autorización de Recursos")
                        .description("API REST con catálogo de productos, favoritos y listas. "
                                + "Incluye autenticación mediante JWT (JSON Web Tokens), registro de usuarios, "
                                + "y autorización basada en roles (USER, ADMIN) y propiedad de recursos.")
                        .version("v3"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
