package se.edugrade.winerater.exception;

public class ExternalWeatherException extends RuntimeException {
    public ExternalWeatherException(String message, Throwable cause) {
        super(message, cause);
    }
}
