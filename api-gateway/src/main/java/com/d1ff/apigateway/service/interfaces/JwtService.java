package com.d1ff.apigateway.service.interfaces;

import java.util.Map;

public interface JwtService {
    Map<String, Object> validateAndGetClaims(String token);
}
