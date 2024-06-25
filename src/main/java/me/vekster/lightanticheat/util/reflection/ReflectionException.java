package me.vekster.lightanticheat.util.reflection;

public class ReflectionException extends Exception {

    public ReflectionException() {
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(Throwable throwable) {
        super(throwable);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
