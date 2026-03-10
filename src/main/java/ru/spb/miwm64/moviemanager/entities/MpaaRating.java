package ru.spb.miwm64.moviemanager.entities;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public enum MpaaRating {

    PG_13(1),
    R(2),
    NC_17(3);

    private final int code;

    private static final Map<Integer, MpaaRating> BY_CODE = new HashMap<>();

    static {
        for (MpaaRating r : values()) {
            BY_CODE.put(r.code, r);
        }
    }

    MpaaRating(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MpaaRating fromCode(int code) {
        return BY_CODE.get(code);
    }

    public static MpaaRating fromString(String input) {
        Objects.requireNonNull(input);
        input = input.trim();

        try {
            int code = Integer.parseInt(input);
            MpaaRating r = fromCode(code);
            if (r != null) {
                return r;
            }
        } catch (NumberFormatException ignored) {}

        return MpaaRating.valueOf(input.toUpperCase());
    }
}