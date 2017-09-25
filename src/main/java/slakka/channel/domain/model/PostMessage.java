package slakka.channel.domain.model;

import java.io.Serializable;

public class PostMessage implements Serializable {

    private final String content;

    public PostMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
