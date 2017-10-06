package slakka.channel.manager.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import slakka.channel.actor.ChannelActor;
import slakka.channel.domain.command.ChannelCommand;
import slakka.channel.exception.ChannelNotFoundException;
import slakka.channel.manager.domain.command.ChannelManagerCommand;
import slakka.channel.manager.domain.command.CreateChannel;
import slakka.channel.manager.domain.event.ChannelManagerEvent;
import slakka.channel.manager.domain.state.ChannelManagerState;

import java.util.List;
import java.util.UUID;

public final class ChannelManagerActor extends AbstractPersistentActor {

    private final int snapshotInterval = 1000;

    // protocols
    public static class GetChannels {}
    public static class SendCommandToChannel {
        private final UUID id;
        private final ChannelCommand command;
        public SendCommandToChannel(final UUID id, final ChannelCommand command) {
            this.id = id;
            this.command = command;
        }
        UUID getId() {
            return id;
        }
        ChannelCommand getCommand() {
            return command;
        }
    }
    public static class GetChannelMessages {
        private final UUID id;
        public GetChannelMessages(final UUID id) {
            this.id = id;
        }
        UUID getId() {
            return id;
        }
    }

    private ChannelManagerState state = new ChannelManagerState();

    private ChannelManagerActor() {}

    @Override
    public void preStart() {
        getSelf().tell(new CreateChannel("general"), getSelf());
    }

    @Override
    public String persistenceId() {
        return "channel-manager";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(ChannelManagerEvent.class, event -> this.state.applyEvent(event, getContext()))
                .match(SnapshotOffer.class, ss -> this.state = (ChannelManagerState) ss.snapshot())
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChannelManagerCommand.class, this::handleChannelManagerCommand)
                .match(GetChannels.class, this::handleGetChannels)
                .match(SendCommandToChannel.class, this::handleSendCommand)
                .match(GetChannelMessages.class, this::handleGetChannelMessages)
                .build();
    }

    private void handleChannelManagerCommand(final ChannelManagerCommand channelManagerCommand) {
        List<ChannelManagerEvent> events = this.state.handleCommand(channelManagerCommand);
        persistAll(events, (event) -> {
            this.state.applyEvent(event, getContext());
            getContext().getSystem().eventStream().publish(event);
            if (lastSequenceNr() % this.snapshotInterval == 0 && lastSequenceNr() != 0) {
                saveSnapshot(this.state.copy());
            }
        });
    }

    private void handleGetChannels(final GetChannels getChannels) {
        getSender().tell(this.state.getChannels(), getSelf());
    }

    private void handleSendCommand(final SendCommandToChannel sendCommandToChannel) {
        ActorRef channel = getContext().findChild(sendCommandToChannel.getId().toString())
                .orElseThrow(ChannelNotFoundException::new);
        channel.forward(sendCommandToChannel.getCommand(), getContext());
    }

    private void handleGetChannelMessages(final GetChannelMessages getChannelMessages) {
        ActorRef channel = getContext().findChild(getChannelMessages.getId().toString())
                .orElseThrow(ChannelNotFoundException::new);
        channel.forward(new ChannelActor.GetMessages(), getContext());
    }

    public static Props props() {
        return Props.create(ChannelManagerActor.class);
    }
}
