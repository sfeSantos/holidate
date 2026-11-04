package io.github.sfesantos.holidays.format;

/**
 * A file could not be read as holiday data, or layers could not be written.
 * {@code line} and {@code column} are 1-based and point at the offending
 * token; both are 0 when the problem is not tied to a position.
 */
public final class HolidayFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int line;
    private final int column;

    public HolidayFormatException(String message) {
        super(message);
        this.line = 0;
        this.column = 0;
    }

    public HolidayFormatException(String message, int line, int column) {
        super("line " + line + ", column " + column + ": " + message);
        this.line = line;
        this.column = column;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
