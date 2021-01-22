package io.github.nnkwrik.astah.exception;

/**
 * astah-apiから投げ出される例外
 *
 * @author Reika Nonokawa
 */
public class AstahAPIRuntimeException extends RuntimeException {

    public AstahAPIRuntimeException() {
        super();
    }

    public AstahAPIRuntimeException(String message) {
        super(message);
    }

    public AstahAPIRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AstahAPIRuntimeException(Throwable cause) {
        super(cause);
    }
}
