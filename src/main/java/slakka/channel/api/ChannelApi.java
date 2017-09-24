package slakka.channel.api;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.japi.pf.PFBuilder;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import slakka.channel.actor.ChannelManager;

import java.util.concurrent.TimeUnit;

public class ChannelApi extends AllDirectives {

    private final ActorRef channelManager;

    public ChannelApi(ActorRef channelManager) {
        this.channelManager = channelManager;
    }

    public Route createRoute() {
        return path("channels", () ->
                route(
                        get(() -> {
                            final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
                            return onComplete(
                                    () -> PatternsCS.ask(this.channelManager, new ChannelManager.GetChannels(), timeout),
                                    maybeResult -> maybeResult
                                            .map(result -> complete(StatusCodes.OK, result, Jackson.marshaller()))
                                            .recover(
                                                    new PFBuilder<Throwable, Route>()
                                                            .matchAny(ex ->
                                                                    complete(StatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage()))
                                                            .build())
                                            .get()
                            );
                        }),
                        post(() -> complete("Hello"))
                )
        );
    }

}
