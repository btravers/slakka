package slakka.api.channel.model;

import java.io.Serializable;

public class Message implements Serializable {

    private String content;

    public void setContent(final String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
