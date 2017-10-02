package slakka.channel.manager.domain.event;

import slakka.channel.manager.domain.model.Channel;

public class ChannelCreated implements  ChannelManagerEvent {

    private final Channel channel;

    public ChannelCreated(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

}
