package org.scge.c2m2.validation;

/**
 * Validation issue severity levels.
 */
public enum ValidationLevel {
    /**
     * Critical issues that prevent valid C2M2 submission.
     */
    ERROR,
    
    /**
     * Issues that may cause problems but don't prevent submission.
     */
    WARNING,
    
    /**
     * Informational messages about the validation process.
     */
    INFO
}