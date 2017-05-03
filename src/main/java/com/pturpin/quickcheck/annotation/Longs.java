package com.pturpin.quickcheck.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.LongPredicate;

/**
 * Created by turpif on 02/05/17.
 */
public final class Longs {
  private Longs() {
    /* Nothing */
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Extra {
    long[] values() default {
        Long.MAX_VALUE,
        Long.MIN_VALUE,
        0};
    double rate() default 0.05;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Exclude {
    long[] value();
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface OpenRange {
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Filter {
    Class<? extends LongPredicate> value();
  }
}
