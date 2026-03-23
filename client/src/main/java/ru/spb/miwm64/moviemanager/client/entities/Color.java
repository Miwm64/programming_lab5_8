package ru.spb.miwm64.moviemanager.client.entities;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public enum Color {
    GREEN(1),
    RED(2),
    YELLOW(3),
    ORANGE(4),
    BROWN(5);

    private final int code;
    private static final Map<Integer, Color> BY_CODE = new HashMap<>();

    static {
        for (Color c : values()) {
            BY_CODE.put(c.code, c);
        }
    }

    Color(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Color fromCode(int code) {
        return BY_CODE.get(code);
    }

    public static Color fromString(String input) {
        Objects.requireNonNull(input);
        input = input.trim();

        try {
            int code = Integer.parseInt(input);
            Color c = fromCode(code);
            if (c != null) {
                return c;
            }
        } catch (NumberFormatException ignored) {}


        return Color.valueOf(input.toUpperCase());
    }
}