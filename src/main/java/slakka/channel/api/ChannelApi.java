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
import slakka.channel.domain.command.AddPersonMessage;
import slakka.channel.domain.model.PostMessage;
import slakka.channel.exception.ChannelNotFoundException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.PathMatchers.uuidSegment;

public class ChannelApi extends AllDirectives {


    private final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    private final ActorRef channelManager;

    public ChannelApi(ActorRef channelManager) {
        this.channelManager = channelManager;
    }

    public Route createRoute() {
        return pathPrefix(segment("channels"), () -> route(
                pathEnd(() -> route(
                        get(this::handleGetChannels),
                        options(() -> complete(StatusCodes.OK))
                )),
                pathPrefix(uuidSegment(), id ->
                        pathPrefix(segment("messages"), () -> route(
                                get(() -> this.handleGetMassagesForChannel(id)),
                                post(() -> entity(
                                        Jackson.unmarshaller(PostMessage.class),
                                        postMessage -> this.handlePostMessageForChannel(id, postMessage)
                                )),
                                options(() -> complete(StatusCodes.OK))
                        ))
                )
        ));
    }

    private Route handleGetChannels() {
        return onComplete(
                () -> PatternsCS.ask(this.channelManager, new ChannelManager.GetChannels(), timeout),
                maybeResult -> maybeResult
                        .map(result -> complete(StatusCodes.OK, result, Jackson.marshaller()))
                        .recover(new PFBuilder<Throwable, Route>()
                                .match(ChannelNotFoundException.class, ex -> complete(StatusCodes.NOT_FOUND))
                                .matchAny(ex -> complete(StatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage()))
                                .build())
                        .get()
        );
    }

    private Route handleGetMassagesForChannel(UUID id) {
        return onComplete(
                () -> PatternsCS.ask(this.channelManager, new ChannelManager.GetChannelMessages(id), timeout),
                maybeResult -> maybeResult
                        .map(result -> complete(StatusCodes.OK, result, Jackson.marshaller()))
                        .recover(new PFBuilder<Throwable, Route>()
                                .match(ChannelNotFoundException.class, ex -> complete(StatusCodes.NOT_FOUND))
                                .matchAny(ex -> complete(StatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage()))
                                .build())
                        .get()
        );
    }

    private Route handlePostMessageForChannel(UUID id, PostMessage postMessage) {
        final AddPersonMessage command = new AddPersonMessage("author", postMessage.getContent());
        channelManager.tell(
                new ChannelManager.SendCommand(id, command),
                ActorRef.noSender()
        );
        return complete(StatusCodes.ACCEPTED);
    }

}
