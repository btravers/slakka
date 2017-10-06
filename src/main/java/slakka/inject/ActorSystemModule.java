package slakka.inject;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import slakka.channel.manager.actor.ChannelManagerActor;

public final class ActorSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(ActorSystem.create("slakka"));
    }

    @Provides @Singleton @Named("ChannelManagerActor")
    private ActorRef channelManagerActor(final ActorSystem actorSystem) {
        return actorSystem.actorOf(ChannelManagerActor.props());
    }

}
