package slakka.api;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.google.inject.Inject;
import slakka.api.auth.AuthApi;
import slakka.api.auth.service.AuthService;
import slakka.api.channel.ChannelApi;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Api extends AllDirectives {

    private final ChannelApi channelApi;
    private final AuthApi authApi;
    private final AuthService authService;

    @Inject
    public Api(final AuthApi authApi, final ChannelApi channelApi, final AuthService authService) {
        this.authApi = authApi;
        this.channelApi = channelApi;
        this.authService = authService;
    }

    public Route createRoute() {
        return route(
                this.authApi.createRoute(),
                authenticated(this.channelApi::createRoute)
        );
    }

    private Route authenticated(final Function<String, Route> inner) {
        return optionalHeaderValueByName("Authorization", mayBeToken ->
                mayBeToken.flatMap(this.authService::getUser)
                        .map(username -> provide(username, inner))
                        .orElse(complete(StatusCodes.UNAUTHORIZED)));
    }

}
