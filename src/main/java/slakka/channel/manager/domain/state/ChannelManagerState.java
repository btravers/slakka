package slakka.channel.manager.domain.state;

import akka.actor.ActorContext;
import slakka.channel.actor.ChannelActor;
import slakka.channel.manager.domain.command.ChannelManagerCommand;
import slakka.channel.manager.domain.command.CreateChannel;
import slakka.channel.manager.domain.event.ChannelCreated;
import slakka.channel.manager.domain.event.ChannelManagerEvent;
import slakka.channel.manager.domain.model.Channel;

import java.io.Serializable;
import java.util.*;
import java.util.List;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

public class ChannelManagerState implements Serializable {

    private final List<Channel> channels;

    public ChannelManagerState() {
        this.channels = new ArrayList<>();
    }

    public ChannelManagerState(final List<Channel> channels) {
        this.channels = channels;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public ChannelManagerState copy() {
        return new ChannelManagerState(new ArrayList<>(this.channels));
    }

    public List<ChannelManagerEvent> handleCommand(final ChannelManagerCommand command) {
        return Match(command).of(
                Case($(instanceOf(CreateChannel.class)), (createChannel) -> {
                    final String name = createChannel.getName();
                    if (this.channels.stream().noneMatch(channel -> Objects.equals(channel.getName(), name))) {
                        final UUID id = UUID.randomUUID();
                        final Channel channel = new Channel(id, name);
                        final ChannelCreated event = new ChannelCreated(channel);



                        return Collections.singletonList(event);
                    }
                    return Collections.emptyList();
                }),
                Case($(), O -> {
                    throw new RuntimeException();
                })
        );
    }

    public void applyEvent(final ChannelManagerEvent event, final ActorContext context) {
        Match(event).of(
                Case($(instanceOf(ChannelCreated.class)), channelCreated -> {
                    final Channel channel = channelCreated.getChannel();
                    final UUID id = channel.getId();
                    final String name = channel.getName();

                    context.actorOf(ChannelActor.props(id, name), id.toString());
                    context.sender().tell(id, context.self());

                    return this.channels.add(channel);
                }),
                Case($(), o -> {
                    throw new RuntimeException();
                })
        );
    }

}
