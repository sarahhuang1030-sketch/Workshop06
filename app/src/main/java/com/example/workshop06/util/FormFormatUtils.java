package com.example.workshop06.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class FormFormatUtils {

    private FormFormatUtils() {
    }

    public static void attachCanadianPhoneFormatter(EditText editText) {
        if (editText == null) return;

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String digits = s.toString().replaceAll("\\D", "");
                if (digits.length() > 10) {
                    digits = digits.substring(0, 10);
                }

                String formatted;
                if (digits.length() <= 3) {
                    formatted = digits;
                } else if (digits.length() <= 6) {
                    formatted = "(" + digits.substring(0, 3) + ") " + digits.substring(3);
                } else {
                    formatted = "(" + digits.substring(0, 3) + ") "
                            + digits.substring(3, 6) + "-"
                            + digits.substring(6);
                }

                editText.setText(formatted);
                editText.setSelection(formatted.length());

                isFormatting = false;
            }
        });
    }

    public static void attachCanadianPostalCodeFormatter(EditText editText) {
        if (editText == null) return;

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String raw = s.toString().toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (raw.length() > 6) {
                    raw = raw.substring(0, 6);
                }

                String formatted;
                if (raw.length() > 3) {
                    formatted = raw.substring(0, 3) + " " + raw.substring(3);
                } else {
                    formatted = raw;
                }

                editText.setText(formatted);
                editText.setSelection(formatted.length());

                isFormatting = false;
            }
        });
    }
}