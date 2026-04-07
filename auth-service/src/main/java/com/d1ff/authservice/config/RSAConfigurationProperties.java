package com.d1ff.authservice.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@ConfigurationProperties(prefix = "jwt")
@Getter
public class RSAConfigurationProperties {
    private final RSAPrivateKey privateKey;

    public RSAConfigurationProperties(String privateKey) {
        this.privateKey = parseSecretKey(privateKey);
    }

    private RSAPrivateKey parseSecretKey(String secretKey){
        try{
            String privateKeyPayload = secretKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPayload);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }catch (Exception e){
            throw new IllegalArgumentException(e);
        }
    }
}
