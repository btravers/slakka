package slakka.channel.domain.command;

import akka.actor.ActorRef;

public class AddBot implements ChannelCommand {

    private final ActorRef bot;

    public AddBot(ActorRef bot) {
        this.bot = bot;
    }

    public ActorRef getBot() {
        return bot;
    }
}
