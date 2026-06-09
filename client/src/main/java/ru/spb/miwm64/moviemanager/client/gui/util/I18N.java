package ru.spb.miwm64.moviemanager.client.gui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18N {
    private static final ObjectProperty<Locale> locale =
            new SimpleObjectProperty<>(Locale.ENGLISH);

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(Locale locale) {
        I18N.locale.set(locale);
    }

    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public static String get(String key) {
        return ResourceBundle
                .getBundle("messages.messages", getLocale())
                .getString(key);
    }

    public static StringBinding createBinding(String key) {
        return Bindings.createStringBinding(
                () -> get(key),
                localeProperty()
        );
    }
}
