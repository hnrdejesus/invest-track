
package com.github.hnrdejesus.invest_track.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Results in HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates exception with resource type and ID.
     * Example: ResourceNotFoundException("Portfolio", 123L)
     * Message: "Portfolio not found with ID: 123"
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with ID: %d", resourceName, id));
    }

    /**
     * Creates exception with resource type and string identifier.
     * Example: ResourceNotFoundException("Asset", "ticker", "AAPL")
     * Message: "Asset not found with ticker: AAPL"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }

    /**
     * Creates exception with custom message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}