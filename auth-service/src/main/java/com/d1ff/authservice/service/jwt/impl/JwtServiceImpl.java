package com.d1ff.authservice.service.jwt.impl;


import com.d1ff.authservice.config.RSAConfigurationProperties;
import com.d1ff.authservice.dto.response.AccessTokenResponse;
import com.d1ff.authservice.entity.User;
import com.d1ff.authservice.service.jwt.interfaces.JwtService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final RSAConfigurationProperties  rsaConfigurationProperties;

    private RSASSASigner signer;

    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.key-ID}")
    private String keyID;

    @PostConstruct
    void init() {
        signer = new RSASSASigner(rsaConfigurationProperties.getPrivateKey());
    }

    @Override
    public AccessTokenResponse createToken(User user) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp exp = new Timestamp(now.getTime() + accessTokenExpiration.toMillis());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(now)
                .expirationTime(exp)
                .jwtID(UUID.randomUUID().toString())
                .issuer(issuer)
                .claim("userId",  user.getId().toString())
                .claim("role", user.getRole().name())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyID).build(),
                claimsSet);

        try{
            signedJWT.sign(signer);
            log.info("JWT was signed");

            return new AccessTokenResponse(signedJWT.serialize(), exp.toInstant().atOffset(ZoneOffset.UTC));

        } catch (JOSEException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Failed to sign JWT token", e);
        }
    }

}
