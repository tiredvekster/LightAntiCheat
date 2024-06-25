package me.vekster.lightanticheat.util.reflection;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    @Nullable
    public static Class<?> classForName(String name) throws ReflectionException {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        try {
            return Class.forName(name.replace("$version", version));
        } catch (ClassNotFoundException | NullPointerException e) {
            return null;
        }
    }

    @Nullable
    public static Constructor getConstructor(Class<?> aClass) throws ReflectionException {
        try {
            return aClass.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException | NullPointerException e) {
            throw new ReflectionException(e);
        }
    }

    @Nullable
    public static Constructor getConstructor(Class<?> aClass, Class<?>... args) throws ReflectionException {
        try {
            return aClass.getConstructor(args);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException | NullPointerException e) {
            throw new ReflectionException(e);
        }
    }

    @NotNull
    public static Object getClassInstanceViaConstructor(Constructor<?> constructor) throws ReflectionException {
        try {
            return constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException |
                 IllegalAccessException | NullPointerException e) {
            throw new ReflectionException(e);
        }
    }

    @NotNull
    public static Object getClassInstanceViaConstructor(Constructor<?> constructor, Object... args) throws ReflectionException {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException | InstantiationException |
                 IllegalAccessException | NullPointerException e) {
            throw new ReflectionException(e);
        }
    }

    @Nullable
    public static Class<?> getDeclaredInnerClass(Class<?> aClass, String name) throws ReflectionException {
        try {
            for (Class<?> innerClass : aClass.getDeclaredClasses()) {
                if (!innerClass.getSimpleName().equals(name))
                    continue;
                return innerClass;
            }
        } catch (NullPointerException e) {
            throw new ReflectionException(e);
        }
        return null;
    }

    @Nullable
    public static Class<?> getInnerClass(Class<?> aClass, String name) throws ReflectionException {
        try {
            for (Class<?> innerClass : aClass.getClasses()) {
                if (!innerClass.getSimpleName().equals(name))
                    continue;
                return innerClass;
            }
        } catch (NullPointerException e) {
            throw new ReflectionException(e);
        }
        return null;
    }

    @Nullable
    public static Object getDeclaredField(Class<?> aClass, String name) throws ReflectionException {
        if (aClass == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Field field : (aClass.getDeclaredFields())) {
            if (!field.getName().equals(name))
                continue;
            try {
                return field.get(aClass);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("Illegal access", e);
            }
        }
        throw new ReflectionException("Field not found");
    }

    @Nullable
    public static Object getDeclaredField(Object object, String name) throws ReflectionException {
        if (object == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Field field : (object.getClass().getDeclaredFields())) {
            if (!field.getName().equals(name))
                continue;
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("Illegal access", e);
            }
        }
        throw new ReflectionException("Field not found");
    }

    @Nullable
    public static Object getField(Class<?> aClass, String name) throws ReflectionException {
        if (aClass == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Field field : (aClass.getFields())) {
            if (!field.getName().equals(name))
                continue;
            try {
                return field.get(aClass);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("Illegal access", e);
            }
        }
        throw new ReflectionException("Field not found");
    }

    @Nullable
    public static Object getField(Object object, String name) throws ReflectionException {
        if (object == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Field field : (object.getClass().getFields())) {
            if (!field.getName().equals(name))
                continue;
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("Illegal access", e);
            }
        }
        throw new ReflectionException("Field not found");
    }

    @Nullable
    public static Object runDeclaredMethod(Class<?> aClass, String name) throws ReflectionException {
        if (aClass == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Method method : (aClass.getDeclaredMethods())) {
            if (!method.getName().equals(name))
                continue;
            try {
                return method.invoke(aClass);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException(e);
            }
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runDeclaredMethod(Object object, String name) throws ReflectionException {
        if (object == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Method method : (object.getClass().getDeclaredMethods())) {
            if (!method.getName().equals(name))
                continue;
            try {
                return method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException(e);
            }
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runDeclaredMethod(Class<?> aClass, String name, Object... args) throws ReflectionException {
        if (aClass == null || name == null || name.isEmpty() || args == null)
            throw new ReflectionException("A method argument is null or blank");
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
            classes[i] = args[i].getClass();
        try {
            aClass.getDeclaredMethod(name, classes).invoke(aClass, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runDeclaredMethod(Object object, String name, Object... args) throws ReflectionException {
        if (object == null || name == null || name.isEmpty() || args == null)
            throw new ReflectionException("A method argument is null or blank");
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
            classes[i] = args[i].getClass();
        try {
            object.getClass().getDeclaredMethod(name, classes).invoke(object, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runMethod(Class<?> aClass, String name) throws ReflectionException {
        if (aClass == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Method method : (aClass.getMethods())) {
            if (!method.getName().equals(name))
                continue;
            try {
                return method.invoke(aClass);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException(e);
            }
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runMethod(Object object, String name) throws ReflectionException {
        if (object == null || name == null || name.isEmpty())
            throw new ReflectionException("A method argument is null or blank");
        for (Method method : (object.getClass().getMethods())) {
            if (!method.getName().equals(name))
                continue;
            try {
                return method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException(e);
            }
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runMethod(Class<?> aClass, String name, Object... args) throws ReflectionException {
        if (aClass == null || name == null || name.isEmpty() || args == null)
            throw new ReflectionException("A method argument is null or blank");
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
            classes[i] = args[i].getClass();
        try {
            aClass.getMethod(name, classes).invoke(aClass, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
        throw new ReflectionException();
    }

    @Nullable
    public static Object runMethod(Object object, String name, Object... args) throws ReflectionException {
        if (object == null || name == null || name.isEmpty() || args == null)
            throw new ReflectionException("A method argument is null or blank");
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
            classes[i] = args[i].getClass();
        try {
            object.getClass().getMethod(name, classes).invoke(object, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
        throw new ReflectionException();
    }

}
