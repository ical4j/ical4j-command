package org.ical4j.command.ops;

/**
 * Typed failure of an operation. Surfaces map each subclass to their own convention: the CLI to
 * an exit code and a stderr line, MCP to an {@code isError} tool result.
 */
public class OperationException extends RuntimeException {

    public OperationException(String message) {
        super(message);
    }

    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /** The named item or locus does not exist. */
    public static class NotFound extends OperationException {
        public NotFound(String message) {
            super(message);
        }
    }

    /** The item or locus already exists and the operation does not replace. */
    public static class AlreadyExists extends OperationException {
        public AlreadyExists(String message) {
            super(message);
        }
    }

    /** Parameters were missing or malformed. Surfaces treat this as a usage error. */
    public static class Usage extends OperationException {
        public Usage(String message) {
            super(message);
        }
    }

    /** Input data failed to parse or validate. */
    public static class InvalidData extends OperationException {
        public InvalidData(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** The backing store failed. */
    public static class Store extends OperationException {
        public Store(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
