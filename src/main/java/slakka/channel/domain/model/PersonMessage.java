package slakka.channel.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class PersonMessage extends Message {

    private final String author;

    public PersonMessage(UUID id, LocalDate date, String content, String author) {
        super(id, date, content);
        this.author = author;
    }

    public PersonMessage(String content, String author) {
        super(UUID.randomUUID(), LocalDate.now(), content);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }
}
