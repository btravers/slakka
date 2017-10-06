package slakka.channel.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PersonMessage extends Message {

    private final String author;

    public PersonMessage(final UUID id, final long date, final String content, final String author) {
        super(id, date, content);
        this.author = author;
    }

    public PersonMessage(final String content, final String author) {
        super(UUID.randomUUID(), Instant.now().toEpochMilli(), content);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }
}
