package slakka.channel.manager.domain.model;

import java.io.Serializable;
import java.util.UUID;

public class Channel implements Serializable {

    private final UUID id;
    private final String name;

    public Channel(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
