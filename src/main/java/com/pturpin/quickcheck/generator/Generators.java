package com.pturpin.quickcheck.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.function.*;

import static com.google.common.base.Preconditions.checkArgument;

public final class Generators {

  private static final int MAX_FILTER_LOOP = 1000;

  private Generators() {
    /* nothing */
  }

  public static <T> Generator<T> constGen(T value) {
    return re -> value;
  }

  public static Generator<Boolean> coin(double trueRate) {
    return re -> re.nextDouble() <= trueRate;
  }

  public static <T> Generator<T> oneOf(Collection<? extends T> objects) {
    checkArgument(!objects.isEmpty());
    return re -> Iterables.get(objects, re.nextInt(objects.size()));
  }

  @SafeVarargs
  public static <T> Generator<T> oneOf(T... objects) {
    return oneOf(ImmutableList.copyOf(objects));
  }

  public static <T> Generator<T> onGenOf(Collection<Generator<? extends T>> generators) {
    checkArgument(!generators.isEmpty());
    return re -> Iterables.get(generators, re.nextInt(generators.size())).get(re);
  }

  @SafeVarargs
  public static <T> Generator<T> onGenOf(Generator<? extends T>... generators) {
    return onGenOf(ImmutableList.copyOf(generators));
  }

  public static <T> Generator<T> nullable(Generator<? extends T> generator, double nullRate) {
    checkArgument(nullRate >= 0 && nullRate <= 1);
    return re -> re.nextDouble() <= nullRate ? null : generator.get(re);
  }

  public static <T> Generator<T> selection(Generator<? extends T> trueGen, Generator<? extends T> falseGen, Generator<Boolean> boolGen) {
    return re -> boolGen.get(re) ? trueGen.get(re) : falseGen.get(re);
  }

  public static <T> Generator<T> filter(Generator<? extends T> generator, Predicate<T> predicate) {
    return re -> {
      T value;
      int nbLoop = 0;
      do {
        value = generator.get(re);
        nbLoop++;
        if (nbLoop > MAX_FILTER_LOOP) {
          throw reachMaxFilterException();
        }
      } while (!predicate.test(value));
      return value;
    };
  }

  public static Generator<Double> filter(Generator<Double> generator, DoublePredicate predicate) {
    return re -> {
      double value;
      int nbLoop = 0;
      do {
        value = generator.get(re);
        nbLoop++;
        if (nbLoop > MAX_FILTER_LOOP) {
          throw reachMaxFilterException();
        }
      } while (!predicate.test(value)); // FIXME handle infinite loop
      return value;
    };
  }

  public static Generator<Long> filter(Generator<Long> generator, LongPredicate predicate) {
    return re -> {
      long value;
      int nbLoop = 0;
      do {
        value = generator.get(re);
        nbLoop++;
        if (nbLoop > MAX_FILTER_LOOP) {
          throw reachMaxFilterException();
        }
      } while (!predicate.test(value));
      return value;
    };
  }

  public static Generator<Integer> filter(Generator<Integer> generator, IntPredicate predicate) {
    return re -> {
      int value;
      int nbLoop = 0;
      do {
        value = generator.get(re);
        nbLoop++;
        if (nbLoop > MAX_FILTER_LOOP) {
          throw reachMaxFilterException();
        }
      } while (!predicate.test(value));
      return value;
    };
  }

  private static RuntimeException reachMaxFilterException() {
    return new RuntimeException("Generate " + MAX_FILTER_LOOP + " values but none match given filtering predicate");
  }

  public static <T, R> Generator<R> map(Generator<? extends T> generator, Function<T, R> mapper) {
    return re -> mapper.apply(generator.get(re));
  }

}
