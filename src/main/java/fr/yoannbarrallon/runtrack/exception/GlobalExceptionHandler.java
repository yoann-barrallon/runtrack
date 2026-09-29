package fr.yoannbarrallon.runtrack.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import fr.yoannbarrallon.runtrack.plan.TrainingPlanStatus;
import fr.yoannbarrallon.runtrack.run.fit.FitImportException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({BusinessRuleViolationException.class, FitImportException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "Path not found: /" + exception.getResourcePath());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method " + exception.getMethod() + " is not supported for this endpoint");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = String.format("Parameter '%s' should be of type %s",
                exception.getName(),
                exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "unknown");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException() {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, readableRequestMessage(exception));
    }

    @ExceptionHandler({
            MultipartException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest() {
        return buildResponse(HttpStatus.BAD_REQUEST, "Request body or multipart data is invalid");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtExceptions(Exception exception) {
        log.error("Unhandled internal server error", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred");
    }

    private String readableRequestMessage(HttpMessageNotReadableException exception) {
        Throwable cause = findCause(exception, InvalidFormatException.class);
        if (cause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType().isEnum()) {
            return invalidEnumMessage(invalidFormatException);
        }
        UnrecognizedPropertyException unrecognizedPropertyException =
                findCause(exception, UnrecognizedPropertyException.class);
        if (unrecognizedPropertyException != null) {
            return "Unknown field '" + unrecognizedPropertyException.getPropertyName() + "'";
        }
        if (exception.getMessage() != null
                && exception.getMessage().contains(TrainingPlanStatus.class.getSimpleName())) {
            return "Invalid value for 'status'. Allowed values: "
                    + String.join(", ", java.util.Arrays.stream(TrainingPlanStatus.values())
                    .map(Enum::name)
                    .toList());
        }
        return "Request body is invalid";
    }

    private String invalidEnumMessage(InvalidFormatException exception) {
        String field = fieldName(exception);
        String allowedValues = String.join(
                ", ",
                java.util.Arrays.stream(exception.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .toList()
        );
        return "Invalid value for '" + field + "'. Allowed values: " + allowedValues;
    }

    private <T extends Throwable> T findCause(Throwable exception, Class<T> type) {
        Throwable current = exception;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private String fieldName(JsonMappingException exception) {
        return exception.getPath().stream()
                .reduce((first, second) -> second)
                .map(JsonMappingException.Reference::getFieldName)
                .orElse("request");
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}