package org.example.flowmanager.api.controller.handler;

import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.dto.ErrorMessageDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Arrays;

@RestControllerAdvice
public class FlowManagerExceptionHandler {

    @ExceptionHandler(ManagerException.class)
    public ResponseEntity<ErrorMessageDto> handleGlobalException(ManagerException ex) {
        ErrorMessageDto message = new ErrorMessageDto()
                .statusCode(ex.getStatus().value())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .description(Arrays.toString(ex.getStackTrace()));

        return ResponseEntity
                .status(ex.getStatus().value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(message);
    }
}