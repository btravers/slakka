package slakka.channel.manager.domain.command;

public class CreateChannel implements ChannelManagerCommand {

    private final String name;

    public CreateChannel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
