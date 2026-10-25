package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vaccine applied to a pet (SPEC-001). Immutable after creation: a re-vaccination (same normalized
 * name for the same pet) is a new record and the previous one stays as history.
 * Framework free: the validation and the name normalization live here.
 */
public class Vaccine {

    public static final int MAX_NAME_LENGTH = 255;

    private Integer id;
    private final Integer petId;
    private final String name;
    private final String normalizedName;
    private final LocalDate applicationDate;
    private final LocalDate expirationDate;

    private Vaccine(Integer petId, String name, LocalDate applicationDate, LocalDate expirationDate) {
        this.petId = petId;
        this.name = name;
        this.normalizedName = normalize(name);
        this.applicationDate = applicationDate;
        this.expirationDate = expirationDate;
    }

    /**
     * Creates a vaccine, validating name and dates.
     *
     * @param today the current date, coming from an injectable clock
     * @throws VaccineValidationException when any rule is broken
     */
    public static Vaccine register(Integer petId, String name, LocalDate applicationDate,
                                   LocalDate expirationDate, LocalDate today) {
        List<VaccineValidationException.Violation> violations = new ArrayList<>();

        if (normalize(name).isEmpty()) {
            violations.add(new VaccineValidationException.Violation("name", "must not be blank"));
        } else if (name.length() > MAX_NAME_LENGTH) {
            violations.add(new VaccineValidationException.Violation("name",
                "must have at most " + MAX_NAME_LENGTH + " characters"));
        }

        if (applicationDate == null) {
            violations.add(new VaccineValidationException.Violation("applicationDate", "must not be null"));
        } else if (applicationDate.isAfter(today)) {
            violations.add(new VaccineValidationException.Violation("applicationDate", "must not be in the future"));
        }

        if (expirationDate == null) {
            violations.add(new VaccineValidationException.Violation("expirationDate", "must not be null"));
        } else if (applicationDate != null && expirationDate.isBefore(applicationDate)) {
            violations.add(new VaccineValidationException.Violation("expirationDate",
                "must not be before the application date"));
        }

        if (!violations.isEmpty()) {
            throw new VaccineValidationException(violations);
        }
        return new Vaccine(petId, name, applicationDate, expirationDate);
    }

    /**
     * Normalized form of a vaccine name: lower case, without spaces and without hyphens
     * (V10, v-10 and 'V 10' are the same vaccine).
     */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isWhitespace(c) && c != '-') {
                builder.append(c);
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPetId() {
        return this.petId;
    }

    /** The name as typed, kept for display. */
    public String getName() {
        return this.name;
    }

    public String getNormalizedName() {
        return this.normalizedName;
    }

    public LocalDate getApplicationDate() {
        return this.applicationDate;
    }

    public LocalDate getExpirationDate() {
        return this.expirationDate;
    }
}
