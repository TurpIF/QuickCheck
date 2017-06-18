package fr.pturpin.quickcheck.base;

import fr.pturpin.quickcheck.functional.Checked.CheckedFunction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by turpif on 02/05/17.
 */
public final class Reflections {
  private Reflections() {
    /* Nothing */
  }

  public static <T> T newFactory(Class<T> klass) throws ReflectiveOperationException {
    Constructor<T> constructor = klass.getConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  public static <T> T uncheckedNewFactory(Class<T> klass) {
    try {
      return newFactory(klass);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static <K, R> Function<K, R> uncheckedFieldGetter(Class<K> klass, String fieldName) {
    Field field;
    try {
      field = klass.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    field.setAccessible(true);

    return instance -> {
      try {
        return (R) field.get(instance);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static CheckedFunction<Object[], Object, ReflectiveOperationException> invoker(Method method, Object instance) {
    checkInvocation(method, instance);
    return arguments -> method.invoke(instance, (Object[]) arguments);
  }

  public static CheckedFunction<Object[], Object, ReflectiveOperationException> invoker(Method method) {
    return invoker(method, null);
  }

  private static void checkInvocation(Method method, Object instance) {
    checkArgument(instance != null || Modifier.isStatic(method.getModifiers()),
        "Impossible to invoke the non-static method without instance: %s", method);
    checkArgument(instance == null || !Modifier.isStatic(method.getModifiers()),
        "Static methods should not be invoked with an instance object: %s", method);
  }
}
