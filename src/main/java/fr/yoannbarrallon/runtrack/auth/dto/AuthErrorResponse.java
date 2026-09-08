package fr.yoannbarrallon.runtrack.auth.dto;

public record AuthErrorResponse(
        int status,
        String error,
        String message
) {
}
