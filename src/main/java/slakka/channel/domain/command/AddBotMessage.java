package slakka.channel.domain.command;

import akka.actor.ActorRef;

public class AddBotMessage implements ChannelCommand {

    private final ActorRef bot;
    private final String content;

    public AddBotMessage(ActorRef bot, String content) {
        this.bot = bot;
        this.content = content;
    }

    public ActorRef getBot() {
        return bot;
    }

    public String getContent() {
        return content;
    }
}
