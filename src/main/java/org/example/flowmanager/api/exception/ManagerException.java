package org.example.flowmanager.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ManagerException extends RuntimeException {

    private final HttpStatus status;

    public ManagerException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ManagerException(String message, Exception cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ManagerException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }

}

