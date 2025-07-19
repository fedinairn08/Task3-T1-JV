package com.task3.exception;

public class CommandQueueFullException extends RuntimeException {

    public CommandQueueFullException(String message) {
        super(message);
    }
}
