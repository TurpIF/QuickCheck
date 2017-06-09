package fr.pturpin.quickcheck.base;

import fr.pturpin.quickcheck.functional.Checked.CheckedFunction;
import fr.pturpin.quickcheck.functional.Checked.CheckedSupplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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

  public static CheckedSupplier<Object, ReflectiveOperationException> invoker0(Method method, Object instance) {
    checkInvocation(method, instance);
    return () -> method.invoke(instance);
  }

  public static CheckedSupplier<Object, ReflectiveOperationException> invoker0(Method method) {
    return invoker0(method, null);
  }

  public static <T> CheckedFunction<T, Object, ReflectiveOperationException> invoker1(Method method, Object instance) {
    checkInvocation(method, instance);
    return arguments -> method.invoke(instance, arguments);
  }

  public static <T> CheckedFunction<T, Object, ReflectiveOperationException> invoker1(Method method) {
    return invoker1(method, null);
  }

  private static void checkInvocation(Method method, Object instance) {
    checkArgument(instance != null || Modifier.isStatic(method.getModifiers()),
        "Impossible to invoke the non-static method without instance: %s", method);
    checkArgument(instance == null || !Modifier.isStatic(method.getModifiers()),
        "Static methods should not be invoked with an instance object: %s", method);
  }
}
