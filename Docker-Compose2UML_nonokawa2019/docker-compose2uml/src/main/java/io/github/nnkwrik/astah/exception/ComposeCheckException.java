package io.github.nnkwrik.astah.exception;

import lombok.Data;

/**
 * チェッカーでエラーが起きた時に投げる例外
 *
 * @author Reika Nonokawa
 */
@Data
public class ComposeCheckException extends Exception {

    private Level level;
    private Type type;

    public ComposeCheckException(Type type, String message) {
        super(message);
        this.level = Level.NONE;
        this.type = type;
    }

    public ComposeCheckException(Type type, Level level, String message) {
        super(message);
        this.level = level;
        this.type = type;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (level == Level.WARNING) {
            message = "WARNING: " + message;
        } else if (level == Level.ERROR) {
            message = "ERROR: " + message;
        }
        return message;
    }



    public enum Type {
        REFERENCE, VALIDATION
    }

    public enum Level {
        NONE, WARNING, ERROR
    }
}
