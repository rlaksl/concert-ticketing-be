package com.ticket.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("콘서트 티켓팅 API")
                        .description("콘서트 티켓 예매 시스템 API 문서")
                        .version("v1.0.0"));
    }
}