package slakka.api.model;

import java.io.Serializable;

public class Message implements Serializable {

    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
