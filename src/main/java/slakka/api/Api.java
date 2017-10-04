package slakka.api;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.google.inject.Inject;
import slakka.api.auth.AuthApi;
import slakka.api.channel.ChannelApi;

public class Api extends AllDirectives {

    private final ChannelApi channelApi;
    private final AuthApi authApi;

    @Inject
    public Api(ChannelApi channelApi, AuthApi authApi) {
        this.channelApi = channelApi;
        this.authApi = authApi;
    }

    public Route createRoute() {
        return route(
                this.channelApi.createRoute(),
                this.authApi.createRoute()
        );
    }

}
