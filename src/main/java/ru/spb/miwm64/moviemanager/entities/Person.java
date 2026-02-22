package ru.spb.miwm64.moviemanager.entities;

import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import java.util.Objects;
// TODO toString, equals
public class Person implements Comparable<Person> {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private float weight; //Значение поля должно быть больше 0
    private Color hairColor; //Поле не может быть null
    private Country nationality; //Поле не может быть null

    public Person(Color hairColor, String name, Country nationality, float weight) {
        this.hairColor = hairColor;
        this.name = name;
        this.nationality = nationality;
        this.weight = weight;
    }

    public Color getHairColor() {
        return hairColor;
    }

    public void setHairColor(Color hairColor) {
        this.hairColor = Objects.requireNonNull(hairColor);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.isEmpty()){
            throw new InvalidValueException("name can not be empty");
        }
        this.name = Objects.requireNonNull(name);
    }

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country nationality) {
        this.nationality = Objects.requireNonNull(nationality);
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        if (weight <= 0){
            throw new InvalidValueException("weight must be greater than 0");
        }
        this.weight = weight;
    }

    @Override
    public int compareTo(Person o) {
        if (this.weight < o.weight){
            return -1;
        }
        if (this.weight > o.weight){
            return 1;
        }
        return 0;
    }
}
