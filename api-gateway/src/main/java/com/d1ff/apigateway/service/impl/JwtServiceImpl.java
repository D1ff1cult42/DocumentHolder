package com.d1ff.apigateway.service.impl;

import com.d1ff.apigateway.config.RsaPropertiesConfig;
import com.d1ff.apigateway.exceptions.TokenValidationException;
import com.d1ff.apigateway.service.interfaces.JwtService;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    private final RsaPropertiesConfig rsaPropertiesConfig;

    @Override
    public Map<String, Object> validateAndGetClaims(String token){
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaPropertiesConfig.getPublicKey());

            if (!signedJWT.verify(verifier)) {
                log.error("Invalid JWT signature");
                throw new TokenValidationException("Invalid JWT signature");
            }

            Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expTime == null) {
                log.error("JWT token has no expiration time");
                throw new TokenValidationException("JWT token has no expiration time");
            }

            Timestamp expirationTime = new Timestamp(expTime.getTime());
            if (expirationTime.before(new Timestamp(System.currentTimeMillis()))) {
                log.warn("JWT token has expired");
                throw new TokenValidationException("JWT token has expired");
            }
            return signedJWT.getJWTClaimsSet().getClaims();
        }catch(TokenValidationException e){
            throw e;
        }catch (Exception e){
            log.error("Error validating JWT token: {}", e.getMessage());
            throw new TokenValidationException(e.getMessage());
        }
    }
}
