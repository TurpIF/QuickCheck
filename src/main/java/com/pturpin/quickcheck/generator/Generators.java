package com.pturpin.quickcheck.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

public class Generators {

  public static <T> Generator<T> constGen(T value) {
    return re -> value;
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
      do {
        value = generator.get(re);
      } while (!predicate.test(value)); // FIXME handle infinite loop
      return value;
    };
  }

  public static <T, R> Generator<R> map(Generator<? extends T> generator, Function<T, R> mapper) {
    return re -> mapper.apply(generator.get(re));
  }

}
