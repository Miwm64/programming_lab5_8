package ru.spb.miwm64.moviemanager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MyComparator<T extends Number> implements Comparator<T> {
    private T value1;
    @Override
    public int compare(T o1, T o2) {
        return 0;
    }


    public <G extends Number> int compare(G o2) {
        if (value1.getClass() == o2.getClass()) {
            if (value1.floatValue() >  o2.floatValue()) {
                return 1;
            }
            if (value1.floatValue() == o2.floatValue()) {
                return 0;
            }
            return -1;
        }
        return 100;
    }

    public int compare(T o1, Double o2) {
        this.value1 = o1;
        return compare(o2);
    }

    void setValue1(T value1) {
        this.value1 = value1;
    }

    public String getMyClass() {
        return this.value1.getClass().getSimpleName();
    }

    public ArrayList<?> getValClass(ArrayList<?> arr) {
        return new ArrayList<>(Arrays.asList(arr.get(0).getClass().getSimpleName()));
    }
}
