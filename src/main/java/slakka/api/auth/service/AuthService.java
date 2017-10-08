package slakka.api.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.Key;
import java.util.Optional;

public final class AuthService {

    private static final Key KEY = MacProvider.generateKey();

    public Optional<String> getUser(final String token) {
        try {
            return Optional.of(
                    Jwts.parser()
                            .setSigningKey(KEY)
                            .parseClaimsJws(token)
                            .getBody()
                            .getSubject()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String generateToken(final String username) {
        return Jwts.builder()
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS512, KEY)
                .compact();
    }


}
