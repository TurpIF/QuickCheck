package fr.pturpin.quickcheck.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.DoublePredicate;

/**
 * Created by turpif on 02/05/17.
 */
public final class Doubles {
  private Doubles() {
    /* Nothing */
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Extra {
    double[] values() default {
        Double.NaN,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.MIN_VALUE,
        Double.MAX_VALUE,
        Double.MIN_NORMAL,
        0.d,
        -0.d};
    double rate() default 0.05;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Exclude {
    double[] value();
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface IncludeNaN {
    double rate() default 0.05;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Range {
    double min() default Double.POSITIVE_INFINITY;
    double max() default Double.NEGATIVE_INFINITY;
    boolean minIsOpen() default false;
    boolean maxIsOpen() default false;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Filter {
    Class<? extends DoublePredicate> value();
  }

}
