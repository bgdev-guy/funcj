package io.github.jfunk;

import java.util.Optional;

public record ParseError(int position, String description, Optional<ParseError> nextError) {

    public ParseError(int postion, String description) {
        this(postion, description, Optional.empty());
    }

}
