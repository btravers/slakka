package slakka.channel.domain.model;

import akka.actor.ActorRef;

import java.time.Instant;
import java.util.UUID;

public class BotMessage extends Message {

    private ActorRef bot;

    public BotMessage(final UUID id, final long date, final String content, final ActorRef bot) {
        super(id, date, content);
        this.bot = bot;
    }

    public BotMessage(final String content, final ActorRef bot) {
        super(UUID.randomUUID(), Instant.now().toEpochMilli(), content);
        this.bot = bot;
    }

    public ActorRef getBot() {
        return bot;
    }
}
