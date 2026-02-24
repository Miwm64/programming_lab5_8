package ru.spb.miwm64.moviemanager.command;

import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class Parameter<T> implements Cloneable{
    private final String name;
    private final String prompt;
    private final Function<String, T> parser;
    private final Predicate<T> validator;
    private final T defaultValue;
    private final boolean required;
    private T value;
    private boolean isSet = false;

    public Parameter(String name, String prompt, Function<String, T> parser,
                 Predicate<T> validator, T defaultValue, boolean required) {
        this.name = name;
        this.prompt = prompt;
        this.parser = parser;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public void fromString(String input) {
        if (Objects.isNull(input) || input.trim().isEmpty()) {
            if (!required){
                value = defaultValue;
                isSet = true;
                return;
            }
            isSet = false;
            throw new InvalidValueException(name + " can't be empty");
        }
        T val;
        try {
            val = parser.apply(input);
        }
        catch (Exception e){
            isSet = false;
            throw new InvalidValueException(name + " couldn't be parsed, input = " + input);
        }

        if (!validator.test(val)){
            isSet = false;
            throw new InvalidValueException(name + " couldn't be validated, input = " + input);
        }
        this.isSet = true;
        this.value = val;
    }

    public String getName() {
        return name;
    }

    public String getPrompt() {
        return prompt;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public T getValue() {
        return value;
    }

    public boolean isSet() {
        return isSet;
    }


    @Override
    public Parameter<T> clone() {
        try {
            Parameter clone = (Parameter) super.clone();
            clone.value = this.value;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
