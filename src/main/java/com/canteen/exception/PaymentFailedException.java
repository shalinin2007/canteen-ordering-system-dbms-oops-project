package com.canteen.exception;

/**
 * Exception thrown when payment processing or verification fails.
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
