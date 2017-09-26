package slakka.channel.domain.model;

import java.io.Serializable;

public class PostMessage implements Serializable {

    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
