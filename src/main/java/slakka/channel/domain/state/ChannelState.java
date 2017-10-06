package slakka.channel.domain.state;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import slakka.channel.domain.command.AddBot;
import slakka.channel.domain.command.AddBotMessage;
import slakka.channel.domain.command.AddPersonMessage;
import slakka.channel.domain.command.ChannelCommand;
import slakka.channel.domain.event.BotAdded;
import slakka.channel.domain.event.ChannelEvent;
import slakka.channel.domain.event.MessageAdded;
import slakka.channel.domain.model.BotMessage;
import slakka.channel.domain.model.Message;
import slakka.channel.domain.model.PersonMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

public class ChannelState implements Serializable {

    private final String name;
    private final List<Message> messages;
    private final List<ActorRef> bots;

    public ChannelState(final String name) {
        this.name = name;
        this.messages = new ArrayList<>();
        this.bots = new ArrayList<>();
    }

    public ChannelState(final String name, final List<Message> messages, final List<ActorRef> bots) {
        this.name = name;
        this.messages = messages;
        this.bots = bots;
    }

    public String getName() {
        return name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ActorRef> getBots() {
        return bots;
    }

    public ChannelState copy() {
        return new ChannelState(this.name, new ArrayList<>(this.messages), new ArrayList<>(this.bots));
    }

    public List<ChannelEvent> handleCommand(final ChannelCommand command, final ActorContext context) {
        return Match(command).of(
                Case($(instanceOf(AddPersonMessage.class)), (addPersonMessage) -> {
                    Message message = new PersonMessage(addPersonMessage.getContent(), addPersonMessage.getAuthor());

                    getBots().forEach(bot -> bot.tell(message, context.self()));

                    ChannelEvent event = new MessageAdded(message);
                    return Collections.singletonList(event);
                }),
                Case($(instanceOf(AddBotMessage.class)), (addBotMessage) -> {
                    Message message = new BotMessage(addBotMessage.getContent(), addBotMessage.getBot());
                    ChannelEvent event = new MessageAdded(message);
                    return  Collections.singletonList(event);
                }),
                Case($(instanceOf(AddBot.class)), (addBot) -> {
                    ChannelEvent event = new BotAdded(addBot.getBot());
                    return Collections.singletonList(event);
                }),
                Case($(), O -> {
                    throw new RuntimeException();
                })
        );
    }

    public void applyEvent(final ChannelEvent event) {
        Match(event).of(
                Case($(instanceOf(MessageAdded.class)), messageAdded -> this.messages.add(messageAdded.getMessage())),
                Case($(instanceOf(BotAdded.class)), botAdded -> this.bots.add(botAdded.getBot())),
                Case($(), o -> {
                    throw new RuntimeException();
                })
        );
    }

}
