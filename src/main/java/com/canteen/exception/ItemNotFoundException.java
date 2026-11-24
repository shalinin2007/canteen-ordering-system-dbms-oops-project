package com.canteen.exception;

/**
 * Exception thrown when a requested food item or category is not found.
 */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}
