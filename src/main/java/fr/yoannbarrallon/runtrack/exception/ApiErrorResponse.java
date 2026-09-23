package fr.yoannbarrallon.runtrack.exception;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}
