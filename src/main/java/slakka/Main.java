package slakka;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import slakka.channel.manager.actor.ChannelManagerActor;
import slakka.channel.api.ChannelApi;

import java.util.concurrent.CompletionStage;

public class Main {

    public static void main(String... args) throws Exception {
        final ActorSystem system = ActorSystem.create("slakka");

        // CHANNEL MANAGER
        final ActorRef channelManager = system.actorOf(ChannelManagerActor.props());

        // REST API
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final ChannelApi channelApi = new ChannelApi(channelManager);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = channelApi.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                materializer
        );

        System.out.println("Server online at http://localhost:8080/");

        System.out.println("Press RETURN to stop...");
        System.in.read();

        // SHUTDOWN
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

}
