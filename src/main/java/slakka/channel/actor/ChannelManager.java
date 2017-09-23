package slakka.channel.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import slakka.channel.domain.command.ChannelCommand;

import java.util.Optional;
import java.util.UUID;

public class ChannelManager extends AbstractLoggingActor {

    // protocols
    public static class CreateChannel {
        private final String name;
        public CreateChannel(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
    public static class SendCommand {
        private final UUID id;
        private final ChannelCommand command;
        public SendCommand(UUID id, ChannelCommand command) {
            this.id = id;
            this.command = command;
        }
        public UUID getId() {
            return id;
        }
        public ChannelCommand getCommand() {
            return command;
        }
    }


    private ChannelManager() {}

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateChannel.class, this::handleCreateChannel)
                .match(SendCommand.class, this::handleSendCommand)
                .build();
    }

    private void handleCreateChannel(CreateChannel createChannel) {
        final UUID id = UUID.randomUUID();
        getContext().actorOf(ChannelActor.props(id, createChannel.getName()), id.toString());
        sender().tell(id, self());
    }

    private void handleSendCommand(SendCommand sendCommand) {
        Optional.ofNullable(getContext().getChild(sendCommand.getId().toString()))
                .ifPresent(child -> child.forward(sendCommand.getCommand(), getContext()));
    }

    public static Props props() {
        return Props.create(ChannelManager.class);
    }
}
