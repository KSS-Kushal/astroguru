package com.kss.astrologer.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Astrosevaa API",
                version = "v1.0",
                description = "API for Astrosevaa application",
                contact = @Contact(name = "Kushal", email = "contact.webweavecreations@gmail.com"),
                license = @License(name = "MIT")
        )
)
@Configuration
public class SwaggerConfig {
}
