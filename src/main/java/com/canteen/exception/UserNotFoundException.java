package com.canteen.exception;

/**
 * Exception thrown when a user cannot be found or credentials are invalid.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
