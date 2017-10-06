package slakka.bot.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import slakka.channel.domain.command.AddBotMessage;
import slakka.channel.domain.model.Message;

public final class BotActor extends AbstractLoggingActor {

    // protocols
    public static class Mute {}
    public static class UnMute {}

    private final Receive talkative = receiveBuilder()
            .match(Mute.class, this::handleMute)
            .match(Message.class, this::handleMessage)
            .build();
    private final Receive mute = receiveBuilder()
            .match(UnMute.class, this::handleUnMute)
            .build();

    private BotActor() {}

    private void handleMute(final Mute mute) {
        log().info("Mute bot");
        getContext().become(this.mute);
    }

    private void handleUnMute(final UnMute unMute) {
        log().info("Unmute bot");
        getContext().become(this.talkative);
    }

    private void handleMessage(final Message message) {
        getSender().tell(new AddBotMessage(self(), "Hello"), getSelf());
    }

    @Override
    public Receive createReceive() {
        return talkative;
    }

    public static Props props() {
        return Props.create(BotActor.class);
    }

}
