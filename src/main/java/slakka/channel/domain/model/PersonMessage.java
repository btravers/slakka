package slakka.channel.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PersonMessage extends Message {

    private final String author;

    public PersonMessage(UUID id, long date, String content, String author) {
        super(id, date, content);
        this.author = author;
    }

    public PersonMessage(String content, String author) {
        super(UUID.randomUUID(), Instant.now().toEpochMilli(), content);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }
}
