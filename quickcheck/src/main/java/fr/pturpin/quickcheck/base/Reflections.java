package fr.pturpin.quickcheck.base;

import java.lang.reflect.Constructor;

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
}
