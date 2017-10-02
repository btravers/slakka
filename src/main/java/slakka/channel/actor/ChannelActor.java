package slakka.channel.actor;

import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import slakka.channel.domain.command.ChannelCommand;
import slakka.channel.domain.event.ChannelEvent;
import slakka.channel.domain.state.ChannelState;

import java.util.List;
import java.util.UUID;

public class ChannelActor extends AbstractPersistentActor {

    private final int snapshotInterval = 1000;

    // protocols
    public static class GetMessages {}

    private final UUID id;
    private ChannelState state;

    private ChannelActor(UUID id, String name) {
        this.id = id;
        this.state = new ChannelState(name);
    }

    @Override
    public String persistenceId() {
        return id.toString();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(ChannelEvent.class, this.state::applyEvent)
                .match(SnapshotOffer.class, ss -> this.state = (ChannelState) ss.snapshot())
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChannelCommand.class, this::handleChannelCommand)
                .match(GetMessages.class, this::handleGetMessages)
                .build();
    }

    private void handleChannelCommand(ChannelCommand channelCommand) {
        List<ChannelEvent> events = this.state.handleCommand(channelCommand, getContext());
        persistAll(events, (event) -> {
            this.state.applyEvent(event);
            getContext().getSystem().eventStream().publish(event);
            if (lastSequenceNr() % this.snapshotInterval == 0 && lastSequenceNr() != 0) {
                saveSnapshot(this.state.copy());
            }
        });
    }

    private void handleGetMessages(GetMessages getMessages) {
        getSender().tell(this.state.getMessages(), getSelf());
    }

    public static Props props(UUID id, String name) {
        return Props.create(ChannelActor.class, id, name);
    }
}
