package ru.spb.miwm64.moviemanager.client.command;

import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Parameter<T> implements Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(Parameter.class);

    private final String name;
    private final String prompt;
    private final Function<String, T> parser;
    private final Predicate<T> validator;
    private final boolean required;
    private T value;
    private boolean isSet = false;

    private boolean isComposite = false;
    private int size = 0;

    public Parameter(String name, String prompt, Function<String, T> parser,
                     Predicate<T> validator, boolean required) {
        this.name = name;
        this.prompt = prompt;
        this.parser = parser;
        this.validator = validator;
        this.required = required;
        LOG.debug("Parameter created: {} (required={})", name, required);
    }

    public Parameter(String name, String prompt, Function<String, T> parser,
                     Predicate<T> validator, boolean required, boolean isComposite, int size) {
        this.name = name;
        this.prompt = prompt;
        this.parser = parser;
        this.validator = validator;
        this.required = required;
        this.isComposite = isComposite;
        this.size = size;
        LOG.debug("Parameter created: {} (required={})", name, required);
    }

    public void fromString(String input) {
        MDC.put("requestId", java.util.UUID.randomUUID().toString());
        try {
            LOG.debug("Parsing input for parameter '{}': {}", name, input);

            if (Objects.isNull(input) || input.trim().isEmpty()) {
                if (!required) {
                    LOG.debug("Optional parameter '{}' left empty", name);
                    return;
                }
                isSet = false;
                LOG.error("Required parameter '{}' is empty", name);
                throw new InvalidValueException(name + " can't be empty");
            }

            T val;
            try {
                val = parser.apply(input);
            } catch (Exception e) {
                isSet = false;
                LOG.error("Parameter '{}' parsing failed, input={}", name, input, e);
                throw new InvalidValueException(name + " couldn't be parsed, input = " + input);
            }

            if (!validator.test(val)) {
                isSet = false;
                LOG.error("Parameter '{}' validation failed, input={}", name, input);
                throw new InvalidValueException(name + " couldn't be validated, input = " + input);
            }

            this.isSet = true;
            this.value = val;
            LOG.debug("Parameter '{}' successfully set to {}", name, value);

        } finally {
            MDC.remove("requestId");
        }
    }

    public String getName() {
        return name;
    }

    public String getPrompt() {
        return prompt;
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

    public boolean isComposite() {
        return isComposite;
    }

    public int compositeSize() {
        return size;
    }

    @Override
    public Parameter<T> clone() {
        try {
            Parameter<T> clone = (Parameter<T>) super.clone();
            clone.value = this.value;
            LOG.debug("Cloned parameter '{}', value={}", name, value);
            return clone;
        } catch (CloneNotSupportedException e) {
            LOG.error("Cloning failed for parameter '{}'", name, e);
            throw new AssertionError();
        }
    }
}