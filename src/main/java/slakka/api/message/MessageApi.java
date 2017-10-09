package slakka.api.message;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.stream.*;
import akka.stream.javadsl.*;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import slakka.api.message.actor.MessageSuscriberActor;
import slakka.channel.domain.event.MessageAdded;

public class MessageApi extends AllDirectives {

    private final ActorSystem actorSystem;

    @Inject
    public MessageApi(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public Route createRoute(final String username) {
        return path("messages", () ->
            handleWebSocketMessages(greeter())
        );
    }

    private Flow<Message, Message, NotUsed> greeter() {

        return Flow.fromGraph(GraphDSL.create((builder) -> {

            SinkShape<Message> fromWebSocket = Sink.<Message>ignore().shape();

            final FlowShape<MessageAdded, Message> toWebSocket = builder.add(Flow.of(MessageAdded.class)
                    .map(messageAdded -> TextMessage.create(messageAdded.getMessage().toString()))
            );

            final Materializer materializer = ActorMaterializer.create(actorSystem);

            final Pair<ActorRef, Publisher<MessageAdded>> messagesSource = Source.<MessageAdded>actorRef(1000, OverflowStrategy.fail())
                    .toMat(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), Keep.both())
                    .run(materializer);

            final ActorRef suscriberActorRef = this.actorSystem.actorOf(MessageSuscriberActor.props(messagesSource.first()));
            this.actorSystem.eventStream().subscribe(suscriberActorRef, MessageAdded.class);

            builder.from(Source.fromPublisher(messagesSource.second()).shape()).via(toWebSocket);

            return FlowShape.of(fromWebSocket.in(), toWebSocket.out());
        }));
    }

}
