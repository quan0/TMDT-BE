package com.mindwell.be.service.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class JwtService {
    private final JwtProperties props;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.algorithm = Algorithm.HMAC256(props.secret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(props.issuer())
                .build();
    }

    public String generateAccessToken(Integer userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessTokenTtlSeconds());

        return JWT.create()
                .withIssuer(props.issuer())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withSubject(String.valueOf(userId))
                .withClaim("email", email)
                .sign(algorithm);
    }

    public Optional<DecodedJWT> verify(String token) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}
