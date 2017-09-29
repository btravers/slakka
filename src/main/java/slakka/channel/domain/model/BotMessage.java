package slakka.channel.domain.model;

import akka.actor.ActorRef;

import java.time.Instant;
import java.util.UUID;

public class BotMessage extends Message {

    private ActorRef bot;

    public BotMessage(UUID id, long date, String content, ActorRef bot) {
        super(id, date, content);
        this.bot = bot;
    }

    public BotMessage(String content, ActorRef bot) {
        super(UUID.randomUUID(), Instant.now().toEpochMilli(), content);
        this.bot = bot;
    }

    public ActorRef getBot() {
        return bot;
    }
}
