package com.timetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CategoryArchivedException extends RuntimeException {

    public CategoryArchivedException(String categoryName) {
        super("Category '" + categoryName + "' is archived and cannot be used");
    }

    public CategoryArchivedException(String message, Throwable cause) {
        super(message, cause);
    }
}