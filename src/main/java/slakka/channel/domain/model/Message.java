package slakka.channel.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public abstract class Message implements Serializable {

    private final UUID id;
    private final long date;
    private final String content;

    protected Message(final UUID id, final long date, final String content) {
        this.id = id;
        this.date = date;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
