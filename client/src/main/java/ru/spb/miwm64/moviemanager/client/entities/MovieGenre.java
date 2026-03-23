package ru.spb.miwm64.moviemanager.client.entities;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public enum MovieGenre {
    DRAMA(1),
    MUSICAL(2),
    TRAGEDY(3),
    THRILLER(4);

    private final int code;
    private static final Map<Integer, MovieGenre> BY_CODE = new HashMap<>();

    static {
        for (MovieGenre g : values()) {
            BY_CODE.put(g.code, g);
        }
    }

    MovieGenre(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MovieGenre fromCode(int code) {
        return BY_CODE.get(code);
    }

    public static MovieGenre fromString(String input) {
        Objects.requireNonNull(input);
        input = input.trim();

        try {
            int code = Integer.parseInt(input);
            MovieGenre g = fromCode(code);
            if (g != null) {
                return g;
            }
        } catch (NumberFormatException ignored) {}

        return MovieGenre.valueOf(input.toUpperCase());
    }
}