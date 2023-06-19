package io.zahori.scheduler;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    // 400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> responseBody = new HashMap<>();

        MethodArgumentNotValidException me = (MethodArgumentNotValidException) e;
        me.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            responseBody.put(fieldName, message);
        });

        return throwException(HttpStatus.BAD_REQUEST, responseBody.toString(), responseBody, e, request);
    }

    // 404
    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> notFound(Exception e, WebRequest request) {
        return throwException(HttpStatus.NOT_FOUND, String.format("Resource not found: %s", e.getMessage()), "Resource not found", e, request);
    }

    // 404
    @ExceptionHandler(value = {CronExpressionException.class})
    protected ResponseEntity<Object> invalidCronExpression(Exception e, WebRequest request) {
        return throwException(HttpStatus.BAD_REQUEST, String.format("Invalid cron expression: %s", e.getMessage()), "Invalid cron expression", e, request);
    }

    // 409
    @ExceptionHandler(value = {ConflictException.class})
    protected ResponseEntity<Object> conflict(Exception e, WebRequest request) {
        return throwException(HttpStatus.CONFLICT, String.format("Resource already exists: %s", e.getMessage()), "Resource already exists", e, request);
    }

    // 500
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> generalException(Exception e, WebRequest request) {
        return throwException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "Internal error", e, request);
    }

    private ResponseEntity<Object> throwException(HttpStatus httpStatus, String internalerror, Object body, Exception e, WebRequest request) {
        LOG.error("[ E ] {}", internalerror);
        return handleExceptionInternal(e, body, new HttpHeaders(), httpStatus, request);
    }
}
