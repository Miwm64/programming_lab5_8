package ru.spb.miwm64.moviemanager.entities;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public enum Country {
    UNITED_KINGDOM(1),
    CHINA(2),
    INDIA(3),
    ITALY(4),
    THAILAND(5);

    private final int code;
    private static final Map<Integer, Country> BY_CODE = new HashMap<>();

    static {
        for (Country c : values()) {
            BY_CODE.put(c.code, c);
        }
    }

    Country(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Country fromCode(int code) {
        return BY_CODE.get(code);
    }

    public static Country fromString(String input) {
        Objects.requireNonNull(input);
        input = input.trim();

        try {
            int code = Integer.parseInt(input);
            Country c = fromCode(code);
            if (c != null) {
                return c;
            }
        } catch (NumberFormatException ignored) {}

        return Country.valueOf(input.toUpperCase());
    }
}