package br.com.uolcompass.entrypoints.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UOL Compass - Digital Bank API")
                        .description("""
                                REST API for a digital bank with support for:
                                * Individual (CPF) and Business (CNPJ) wallets
                                * Asynchronous fund transfers via SAGA pattern (RabbitMQ)
                                * Wallet statement with password authentication
                                * Idempotency and eventual consistency guarantees
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Erick Cavalcanti")
                                .email("erickfpc@gmail.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://uolcompass.com")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local development"))
                .tags(List.of(
                        new Tag().name("Wallets").description("Wallet creation, query and statement operations"),
                        new Tag().name("Transferences").description("Transference initiation and status query")
                ));
    }
}
