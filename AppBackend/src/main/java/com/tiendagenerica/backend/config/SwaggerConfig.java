package com.tiendagenerica.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Documentacion interactiva de la API (Swagger UI), tal como se presenta en la
 * seccion "Especificacion de la API" del documento del proyecto.
 * Disponible en: http://localhost:5000/swagger-ui/
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tiendagenerica.backend.controlador"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(informacion());
    }

    private ApiInfo informacion() {
        return new ApiInfoBuilder()
                .title("API Tienda Generica Virtual")
                .description("Servicios REST para la gestion de usuarios, clientes, proveedores, "
                        + "productos, ventas, detalle de ventas y reportes.")
                .version("1.0.0")
                .build();
    }
}
