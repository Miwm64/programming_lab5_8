package ru.spb.miwm64.moviemanager.entities;

import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import java.util.Objects;

public class Coordinates {
    private float x; //Максимальное значение поля: 274
    private Long y; //Значение поля должно быть больше -559, Поле не может быть null

    public Coordinates(float x, Long y) {
        setX(x);
        setY(y);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        if (x > 274){
            throw new InvalidValueException("x can not exceed 274");
        }
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        if (y < -559){
            throw new InvalidValueException("y can not be less than -559");
        }
        this.y = Objects.requireNonNull(y);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Float.compare(x, that.x) == 0 && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
