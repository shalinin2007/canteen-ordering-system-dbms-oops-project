package com.canteen.exception;

/**
 * Exception thrown when attempting an operation on an empty cart.
 */
public class CartEmptyException extends RuntimeException {
    public CartEmptyException(String message) {
        super(message);
    }
}
