package slakka;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.google.inject.Guice;
import com.google.inject.Injector;
import slakka.api.Api;
import slakka.inject.ActorSystemModule;

import java.util.concurrent.CompletionStage;

public class Main {

    public static void main(String... args) throws Exception {
        Injector injector = Guice.createInjector(new ActorSystemModule());

        final ActorSystem system = injector.getInstance(ActorSystem.class);
        final Api api = injector.getInstance(Api.class);

        // REST API
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = api.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                materializer
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            binding
                    .thenCompose(ServerBinding::unbind)
                    .thenAccept(unbound -> system.terminate());
        }));
    }

}
