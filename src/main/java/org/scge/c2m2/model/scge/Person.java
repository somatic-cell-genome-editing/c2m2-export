package org.scge.c2m2.model.scge;

/**
 * Represents a person entity from the SCGE database.
 */
public record Person(
    Integer personId,
    String firstName,
    String lastName,
    String email,
    String institution,
    String role
) {
    
    /**
     * Gets the full name of the person.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
    
    /**
     * Checks if this person has sufficient information for C2M2 mapping.
     */
    public boolean isComplete() {
        return personId != null && 
               (firstName != null || lastName != null);
    }
}