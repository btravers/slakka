package slakka.channel.api;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class ChannelApi extends AllDirectives {

    public Route createRoute() {
        return route(
                path("channel", () ->
                        post(() ->
                                complete("Hello")
                        )
                )
        );
    }

}
