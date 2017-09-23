package slakka.channel.domain.command;

public class AddPersonMessage implements ChannelCommand {

    private final String author;
    private final String content;

    public AddPersonMessage(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
