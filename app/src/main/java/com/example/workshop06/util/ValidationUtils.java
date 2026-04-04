package com.example.workshop06.util;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

public class ValidationUtils {

    private ValidationUtils() {
        // Prevent instantiation
    }

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{3,20}$");

    private static final Pattern CANADA_POSTAL_CODE_PATTERN =
            Pattern.compile("^[A-Za-z]\\d[A-Za-z][ -]?\\d[A-Za-z]\\d$");

    public static boolean required(EditText field, String message) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";
        if (TextUtils.isEmpty(value)) {
            field.setError(message);
            field.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean email(EditText field) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";

        if (TextUtils.isEmpty(value)) {
            field.setError("Email is required");
            field.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()
                || !value.matches(".*\\.[A-Za-z]{2,}$")) {
            field.setError("Enter a valid email (e.g. name@example.com)");
            field.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean minLength(EditText field, int minLength, String message) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";
        if (value.length() < minLength) {
            field.setError(message);
            field.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean exactLength(EditText field, int length, String message) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";
        if (value.length() != length) {
            field.setError(message);
            field.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean number(EditText field, String message) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";
        if (TextUtils.isEmpty(value)) {
            field.setError(message);
            field.requestFocus();
            return false;
        }

        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            field.setError(message);
            field.requestFocus();
            return false;
        }
    }

    public static boolean positiveNumber(EditText field, String message) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";
        if (TextUtils.isEmpty(value)) {
            field.setError(message);
            field.requestFocus();
            return false;
        }

        try {
            double number = Double.parseDouble(value);
            if (number <= 0) {
                field.setError(message);
                field.requestFocus();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            field.setError(message);
            field.requestFocus();
            return false;
        }
    }

    public static boolean integer(EditText field, String message) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";
        if (TextUtils.isEmpty(value)) {
            field.setError(message);
            field.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            field.setError(message);
            field.requestFocus();
            return false;
        }
    }

    public static boolean phone(EditText field) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";

        if (TextUtils.isEmpty(value)) {
            field.setError("Phone number is required");
            field.requestFocus();
            return false;
        }

        String digits = value.replaceAll("\\D", "");
        if (digits.length() != 10) {
            field.setError("Enter a valid 10-digit phone number");
            field.requestFocus();
            return false;
        }

        if (!value.matches("^\\(\\d{3}\\) \\d{3}-\\d{4}$")) {
            field.setError("Phone must be in format (123) 456-7890");
            field.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean match(EditText field1, EditText field2, String message) {
        if (field1 == null || field2 == null) return false;

        String value1 = field1.getText() != null ? field1.getText().toString().trim() : "";
        String value2 = field2.getText() != null ? field2.getText().toString().trim() : "";

        if (!value1.equals(value2)) {
            field2.setError(message);
            field2.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean username(EditText field) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim() : "";

        if (TextUtils.isEmpty(value)) {
            field.setError("Username is required");
            field.requestFocus();
            return false;
        }

        if (!USERNAME_PATTERN.matcher(value).matches()) {
            field.setError("Username must be 3-20 characters and use letters, numbers, ., _, or -");
            field.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean canadianPostalCode(EditText field) {
        if (field == null) return false;

        String value = field.getText() != null ? field.getText().toString().trim().toUpperCase() : "";

        if (TextUtils.isEmpty(value)) {
            field.setError("Postal code is required");
            field.requestFocus();
            return false;
        }

        if (!CANADA_POSTAL_CODE_PATTERN.matcher(value).matches()) {
            field.setError("Enter a valid Canadian postal code");
            field.requestFocus();
            return false;
        }

        return true;
    }
}