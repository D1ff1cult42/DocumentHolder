package com.d1ff.apigateway.filter;

import com.d1ff.apigateway.dto.WebFluxErrorResponse;
import com.d1ff.apigateway.exceptions.TokenValidationException;
import com.d1ff.apigateway.service.interfaces.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@Order(-1)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange stripped = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Email");
                    h.remove("X-User-Role");
                }))
                .build();

        String authHeader = stripped.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(stripped);
        }

        String token = authHeader.substring(7);

        Map<String, Object> claims;
        try {
            claims = jwtService.validateAndGetClaims(token);
        } catch (TokenValidationException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token");
        }

        String userId = (String) claims.get("userId");
        String email  = (String) claims.get("sub");
        String role   = (String) claims.get("role");

        ServerHttpRequest mutatedRequest = stripped.getRequest().mutate()
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-User-Email", email != null ? email : "")
                .header("X-User-Role", role != null ? role : "")
                .build();

        log.debug("JWT validated successfully for user: {} (ID: {})", email, userId);

        return chain.filter(stripped.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        WebFluxErrorResponse body = WebFluxErrorResponse.of(status, message, exchange.getRequest().getPath().value());
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
