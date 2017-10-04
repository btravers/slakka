package slakka.api.auth;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import slakka.api.auth.model.LoginRequest;

import java.security.Key;

import static akka.http.javadsl.server.PathMatchers.segment;

public class AuthApi extends AllDirectives {

    private static final Key KEY = MacProvider.generateKey();

    public Route createRoute() {
        return pathPrefix(segment("login"), () -> route(
                post(() -> entity(
                        Jackson.unmarshaller(LoginRequest.class),
                        this::handleLoginRequest
                )),
                options(() -> complete(StatusCodes.OK))
        ));
    }

    private Route handleLoginRequest(LoginRequest loginRequest) {
        String token = Jwts.builder()
                .setSubject(loginRequest.getUsername())
                .signWith(SignatureAlgorithm.HS512, KEY)
                .compact();

        return respondWithHeader(RawHeader.create("Access-Token", token), () ->
                complete(StatusCodes.OK)
        );
    }
}
