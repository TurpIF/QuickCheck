package com.pturpin.quickcheck.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.IntPredicate;

/**
 * Created by turpif on 02/05/17.
 */
public final class Ints {
  private Ints() {
    /* Nothing */
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Extra {
    int[] values() default {
        Integer.MAX_VALUE,
        Integer.MIN_VALUE,
        0};
    double rate() default 0.05;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Exclude {
    int[] value();
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Range {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
    boolean minIsOpen() default false;
    boolean maxIsOpen() default false;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Filter {
    Class<? extends IntPredicate> value();
  }
}
