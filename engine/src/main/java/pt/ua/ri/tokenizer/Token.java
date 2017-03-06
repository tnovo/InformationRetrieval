package pt.ua.ri.tokenizer;

import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author tiago.novo
 */
public class Token {
    private static final AtomicLong number = new AtomicLong(0L);
    private final long id;
    private String string;

    Token(final String s) {
        checkNotNull(s);
        checkArgument(!s.isEmpty());
        this.id = number.getAndIncrement();
        this.string = s;
    }

    public long getId() {
        return id;
    }

    public String getString() {
        return string;
    }

    public void setString(final String string) {
        this.string = string;
    }
}
