package com.lxb.lxbsessential.heal;

import java.lang.reflect.Method;

/** Calls a public method found on the object's runtime class, so mod implementation classes need no compile-time link. */
final class Reflect {

    private Reflect() {
    }

    static Object call(Object target, String name, Class<?>[] parameterTypes, Object... args)
            throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    static Class<?> load(String name) throws ClassNotFoundException {
        return Class.forName(name, false, Reflect.class.getClassLoader());
    }
}
