package slakka.api.channel.model;

import java.io.Serializable;

public class Channel implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
