package slakka.api.message.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import slakka.channel.domain.event.MessageAdded;

public class MessageSubscriberActor extends AbstractLoggingActor {

    private final ActorRef ws;

    private MessageSubscriberActor(final ActorRef ws) {
        this.ws = ws;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MessageAdded.class, messageAdded -> ws.tell(messageAdded, ActorRef.noSender()))
                .build();
    }

    public static Props props(ActorRef ws) {
        return Props.create(MessageSubscriberActor.class, ws);
    }

}
