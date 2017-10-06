package slakka.api.auth;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.google.inject.Inject;
import slakka.api.auth.model.LoginRequest;
import slakka.api.auth.service.AuthService;

import static akka.http.javadsl.server.PathMatchers.segment;

public final class AuthApi extends AllDirectives {

    private final AuthService authService;

    @Inject
    public AuthApi(final AuthService authService) {
        this.authService = authService;
    }

    public Route createRoute() {
        return pathPrefix(segment("login"), () -> route(
                post(() -> entity(
                        Jackson.unmarshaller(LoginRequest.class),
                        this::handleLoginRequest
                )),
                options(() -> complete(StatusCodes.OK))
        ));
    }

    private Route handleLoginRequest(final LoginRequest loginRequest) {
        return respondWithHeader(
                RawHeader.create("Access-Token", this.authService.generateToken(loginRequest.getUsername())),
                () -> complete(StatusCodes.OK)
        );
    }
}
