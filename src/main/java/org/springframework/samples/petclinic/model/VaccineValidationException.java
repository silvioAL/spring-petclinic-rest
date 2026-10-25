package org.springframework.samples.petclinic.model;

import java.util.List;

/**
 * Thrown by the Vaccine aggregate when a registration breaks a validation rule (SPEC-001, section 8).
 * Carries every violated field, so the adapter can answer 400 pointing at each one.
 */
public class VaccineValidationException extends RuntimeException {

    public record Violation(String field, String message) {
    }

    private final List<Violation> violations;

    public VaccineValidationException(List<Violation> violations) {
        super("Invalid vaccine: " + violations);
        this.violations = List.copyOf(violations);
    }

    public List<Violation> getViolations() {
        return this.violations;
    }
}
