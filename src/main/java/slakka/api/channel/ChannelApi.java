package slakka.api.channel;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.japi.pf.PFBuilder;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import slakka.api.channel.model.Channel;
import slakka.api.channel.model.Message;
import slakka.channel.domain.command.AddPersonMessage;
import slakka.channel.exception.ChannelNotFoundException;
import slakka.channel.manager.actor.ChannelManagerActor;
import slakka.channel.manager.domain.command.CreateChannel;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.PathMatchers.uuidSegment;

public final class ChannelApi extends AllDirectives {

    private final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    private final ActorRef channelManager;

    @Inject
    public ChannelApi(@Named("ChannelManagerActor") final ActorRef channelManager) {
        this.channelManager = channelManager;
    }

    public Route createRoute(String username) {
        return pathPrefix(segment("channels"), () -> route(
                pathEnd(() -> route(
                        get(this::handleGetChannels),
                        post(() -> entity(
                                Jackson.unmarshaller(Channel.class),
                                this::handlePostChannels
                        )),
                        options(() -> complete(StatusCodes.OK))
                )),
                pathPrefix(uuidSegment(), id ->
                        pathPrefix(segment("messages"), () -> route(
                                get(() -> this.handleGetMassagesForChannel(id)),
                                post(() -> entity(
                                        Jackson.unmarshaller(Message.class),
                                        message -> this.handlePostMessageForChannel(id, username, message)
                                )),
                                options(() -> complete(StatusCodes.OK))
                        ))
                )
        ));
    }

    private Route handleGetChannels() {
        return onComplete(
                () -> PatternsCS.ask(this.channelManager, new ChannelManagerActor.GetChannels(), timeout),
                maybeResult -> maybeResult
                        .map(result -> complete(StatusCodes.OK, result, Jackson.marshaller()))
                        .recover(new PFBuilder<Throwable, Route>()
                                .match(ChannelNotFoundException.class, ex -> complete(StatusCodes.NOT_FOUND))
                                .matchAny(ex -> complete(StatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage()))
                                .build())
                        .get()
        );
    }

    private Route handlePostChannels(final Channel channel) {
        return onComplete(
                () -> PatternsCS.ask(this.channelManager, new CreateChannel(channel.getName()), timeout),
                maybeResult -> maybeResult
                        .map(result -> complete(StatusCodes.OK, result, Jackson.marshaller()))
                        .recover(new PFBuilder<Throwable, Route>()
                            .matchAny(ex -> complete(StatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage()))
                            .build())
                        .get()
        );
    }

    private Route handleGetMassagesForChannel(final UUID id) {
        return onComplete(
                () -> PatternsCS.ask(this.channelManager, new ChannelManagerActor.GetChannelMessages(id), timeout),
                maybeResult -> maybeResult
                        .map(result -> complete(StatusCodes.OK, result, Jackson.marshaller()))
                        .recover(new PFBuilder<Throwable, Route>()
                                .match(ChannelNotFoundException.class, ex -> complete(StatusCodes.NOT_FOUND))
                                .matchAny(ex -> complete(StatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage()))
                                .build())
                        .get()
        );
    }

    private Route handlePostMessageForChannel(final UUID id, final String username, final Message message) {
        final AddPersonMessage command = new AddPersonMessage(username, message.getContent());
        channelManager.tell(
                new ChannelManagerActor.SendCommandToChannel(id, command),
                ActorRef.noSender()
        );
        return complete(StatusCodes.ACCEPTED);
    }

}
