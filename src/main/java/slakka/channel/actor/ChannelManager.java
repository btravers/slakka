package slakka.channel.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import slakka.channel.domain.command.ChannelCommand;
import slakka.channel.domain.model.Channel;
import slakka.channel.exception.ChannelNotFoundException;

import java.util.*;

public class ChannelManager extends AbstractLoggingActor {

    // protocols
    public static class CreateChannel {
        private final String name;
        public CreateChannel(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }
    }
    public static class GetChannels {}
    public static class SendCommand {
        private final UUID id;
        private final ChannelCommand command;
        public SendCommand(UUID id, ChannelCommand command) {
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
        public GetChannelMessages(UUID id) {
            this.id = id;
        }
        UUID getId() {
            return id;
        }
    }

    private final List<Channel> channels = new ArrayList<>();

    private ChannelManager() {}

    @Override
    public void preStart() {
        final UUID id = UUID.randomUUID();
        final String name = "general";
        this.channels.add(new Channel(id, name));
        getContext().actorOf(ChannelActor.props(id, name), id.toString());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateChannel.class, this::handleCreateChannel)
                .match(GetChannels.class, this::handleGetChannels)
                .match(SendCommand.class, this::handleSendCommand)
                .match(GetChannelMessages.class, this::handleGetChannelMessages)
                .build();
    }

    private void handleCreateChannel(CreateChannel createChannel) {
        final UUID id = UUID.randomUUID();
        final String name = createChannel.getName();
        if (this.channels.stream().noneMatch(channel -> Objects.equals(channel.getName(), name))) {
            this.channels.add(new Channel(id, name));
            getContext().actorOf(ChannelActor.props(id, name), id.toString());
            sender().tell(id, self());
        }
    }

    private void handleGetChannels(GetChannels getChannels) {
        sender().tell(this.channels, getSelf());
    }

    private void handleSendCommand(SendCommand sendCommand) {
        ActorRef channel = Optional.ofNullable(getContext().getChild(sendCommand.getId().toString()))
                .orElseThrow(ChannelNotFoundException::new);
        channel.forward(sendCommand.getCommand(), getContext());
    }

    private void handleGetChannelMessages(GetChannelMessages getChannelMessages) {
        ActorRef channel = Optional.ofNullable(getContext().getChild(getChannelMessages.getId().toString()))
                .orElseThrow(ChannelNotFoundException::new);
        channel.forward(new ChannelActor.GetMessages(), getContext());
    }

    public static Props props() {
        return Props.create(ChannelManager.class);
    }
}
