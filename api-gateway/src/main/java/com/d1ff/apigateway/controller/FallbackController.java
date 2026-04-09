package com.d1ff.apigateway.controller;

import com.d1ff.apigateway.dto.WebFluxErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping("/fallback/{service}")
    public ResponseEntity<WebFluxErrorResponse> fallback(@PathVariable String service,
                                                         ServerWebExchange exchange) {
        log.warn("Circuit breaker fallback triggered for service: {}", service);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(WebFluxErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        service + " is currently unavailable, please try again later",
                        exchange.getRequest().getPath().value()
                ));
    }
}
