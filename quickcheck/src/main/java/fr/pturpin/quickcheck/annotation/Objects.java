package fr.pturpin.quickcheck.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Created by turpif on 28/04/17.
 */
public final class Objects {

  private Objects() { /* Nothing */ }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Filter {
    Class<? extends Predicate<?>> value();
  }
}
