package org.example.server.exception;

public class DeadLineExceedEcxeption extends RuntimeException {
    public DeadLineExceedEcxeption() {
    }

    public DeadLineExceedEcxeption(String message) {
        super(message);
    }

    public DeadLineExceedEcxeption(String message, Throwable cause) {
        super(message, cause);
    }

    public DeadLineExceedEcxeption(Throwable cause) {
        super(cause);
    }

    public DeadLineExceedEcxeption(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
