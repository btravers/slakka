package slakka.channel.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public abstract class Message implements Serializable {

    private final UUID id;
    private final LocalDate date;
    private final String content;

    protected Message(UUID id, LocalDate date, String content) {
        this.id = id;
        this.date = date;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
