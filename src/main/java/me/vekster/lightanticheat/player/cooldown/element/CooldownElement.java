package me.vekster.lightanticheat.player.cooldown.element;

public class CooldownElement<T> {

    public CooldownElement(T result, long time) {
        this.result = result;
        this.time = time;
    }

    public volatile T result;
    public volatile long time;

}
