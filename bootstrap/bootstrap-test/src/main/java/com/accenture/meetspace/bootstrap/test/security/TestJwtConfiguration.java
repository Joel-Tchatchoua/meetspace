package com.accenture.meetspace.bootstrap.test.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@TestConfiguration(proxyBeanMethods = false)
public class TestJwtConfiguration {

    private static final int KEY_SIZE = 2048;

    @Bean
    KeyPair testJwtKeyPair() throws NoSuchAlgorithmException {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }

    @Bean
    JwtDecoder testJwtDecoder(KeyPair testJwtKeyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) testJwtKeyPair.getPublic()).build();
    }

    @Bean
    TestJwts testJwts(KeyPair testJwtKeyPair) {
        var key = new RSAKey.Builder((RSAPublicKey) testJwtKeyPair.getPublic())
                .privateKey((RSAPrivateKey) testJwtKeyPair.getPrivate())
                .build();
        return new TestJwts(new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))));
    }
}
