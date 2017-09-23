package slakka.channel.domain.event;

import akka.actor.ActorRef;

public class BotAdded implements ChannelEvent {

    private final ActorRef bot;

    public BotAdded(ActorRef bot) {
        this.bot = bot;
    }

    public ActorRef getBot() {
        return bot;
    }
}
