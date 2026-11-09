package com.library.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a registered library user (student, faculty, or staff librarian).
 */
public class LibraryUser implements Serializable {
    private static final long serialVersionUID = 2L;

    private String userId;
    private String fullName;
    private String registrationNumber;
    private String department;
    private String contactDetails;
    private boolean active;
    private UserRole role;
    private String username;
    private String password;

    public LibraryUser() {
        this.active = true;
        this.role = UserRole.STUDENT;
        this.password = "student123";
    }

    public LibraryUser(String userId, String fullName, String registrationNumber, String department, String contactDetails) {
        this(userId, fullName, registrationNumber, department, contactDetails, true, UserRole.STUDENT, registrationNumber, "student123");
    }

    public LibraryUser(String userId, String fullName, String registrationNumber, String department, String contactDetails, boolean active) {
        this(userId, fullName, registrationNumber, department, contactDetails, active, UserRole.STUDENT, registrationNumber, "student123");
    }

    public LibraryUser(String userId, String fullName, String registrationNumber, String department, 
                       String contactDetails, boolean active, UserRole role, String username, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.registrationNumber = registrationNumber;
        this.department = department;
        this.contactDetails = contactDetails;
        this.active = active;
        this.role = role != null ? role : UserRole.STUDENT;
        this.username = username != null ? username : registrationNumber;
        this.password = password != null ? password : "password123";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserRole getRole() {
        return role != null ? role : UserRole.STUDENT;
    }

    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.STUDENT;
    }

    public String getUsername() {
        return username != null ? username : registrationNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password != null ? password : "password123";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLibrarian() {
        return getRole().isLibrarian();
    }

    public boolean isStudent() {
        return getRole().isStudent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryUser that = (LibraryUser) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return fullName + " (" + registrationNumber + ") - " + department + 
               " [" + getRole().getDisplayName() + "]" + (active ? "" : " [Deactivated]");
    }
}
