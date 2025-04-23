package com.p2pexchange.util;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security-related operations.
 */
public class SecurityUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Get the current authenticated user's ID.
     * 
     * @return The user ID, or null if not authenticated
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
    
    /**
     * Check if the current user has a specific role.
     * 
     * @param role The role to check for
     * @return True if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
    
    /**
     * Check if the current user is an admin.
     * 
     * @return True if the user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    /**
     * Generate a random token for use in various security contexts.
     * 
     * @param byteLength The number of random bytes to generate
     * @return A Base64-encoded random token
     */
    public static String generateRandomToken(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Generate a secure password.
     * 
     * @param length The password length
     * @return A secure random password
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }
        
        // Define character sets for password complexity
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numericChars = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        
        // Ensure at least one character from each set
        StringBuilder password = new StringBuilder();
        password.append(upperChars.charAt(SECURE_RANDOM.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(SECURE_RANDOM.nextInt(lowerChars.length())));
        password.append(numericChars.charAt(SECURE_RANDOM.nextInt(numericChars.length())));
        password.append(specialChars.charAt(SECURE_RANDOM.nextInt(specialChars.length())));
        
        // Fill the rest with random characters from all sets
        String allChars = upperChars + lowerChars + numericChars + specialChars;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }
        
        // Shuffle the password to avoid predictable pattern
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = SECURE_RANDOM.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Check if a password meets security requirements.
     * 
     * @param password The password to check
     * @return True if the password is secure, false otherwise
     */
    public static boolean isSecurePassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecial;
    }
    
    /**
     * Sanitize user input to prevent XSS attacks.
     * 
     * @param input The input to sanitize
     * @return The sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#39;")
                   .replaceAll("&", "&amp;");
    }
    
    /**
     * Mask sensitive data like email addresses or credit card numbers.
     * 
     * @param data The data to mask
     * @param visibleChars The number of characters to leave visible at the beginning and end
     * @return The masked data
     */
    public static String maskSensitiveData(String data, int visibleChars) {
        if (data == null || data.length() <= visibleChars * 2) {
            return data;
        }
        
        String start = data.substring(0, visibleChars);
        String end = data.substring(data.length() - visibleChars);
        int maskLength = data.length() - (visibleChars * 2);
        
        StringBuilder masked = new StringBuilder(start);
        for (int i = 0; i < maskLength; i++) {
            masked.append('*');
        }
        masked.append(end);
        
        return masked.toString();
    }
    
    /**
     * Mask an email address.
     * 
     * @param email The email to mask
     * @return The masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return email;
        }
        
        String maskedUsername = username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1);
        return maskedUsername + "@" + domain;
    }
}