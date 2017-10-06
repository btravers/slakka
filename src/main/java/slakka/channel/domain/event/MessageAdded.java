package slakka.channel.domain.event;

import slakka.channel.domain.model.Message;

public class MessageAdded implements ChannelEvent {

    private final Message message;

    public MessageAdded(final Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
