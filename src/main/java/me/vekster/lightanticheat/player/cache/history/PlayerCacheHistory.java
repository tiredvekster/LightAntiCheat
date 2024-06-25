package me.vekster.lightanticheat.player.cache.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerCacheHistory<T> {

    public PlayerCacheHistory(T object) {
        HISTORY.addAll(Collections.synchronizedList(new ArrayList<>(Arrays.asList(object, object, object,
                object, object, object, object, object, object, object, object))));
    }

    private final List<T> HISTORY = Collections.synchronizedList(new ArrayList<>());

    public void add(T value) {
        HISTORY.add(value);
        HISTORY.remove(0);
    }

    private T get(int index) {
        return HISTORY.get(index);
    }

    public T get(HistoryElement element) {
        T result = null;
        switch (element) {
            case FROM:
                result = get(10);
                break;
            case FIRST:
                result = get(9);
                break;
            case SECOND:
                result = get(8);
                break;
            case THIRD:
                result = get(7);
                break;
            case FOURTH:
                result = get(6);
                break;
            case FIFTH:
                result = get(5);
                break;
            case SIXTH:
                result = get(4);
                break;
            case SEVENTH:
                result = get(3);
                break;
            case EIGHT:
                result = get(2);
                break;
            case NINTH:
                result = get(1);
                break;
            case TENTH:
                result = get(0);
                break;
        }
        return result;
    }

}
