package fr.pturpin.quickcheck.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import fr.pturpin.quickcheck.base.Reflections;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Generators {

  private static final int MAX_FILTER_LOOP = 1000;

  private Generators() {
    /* nothing */
  }

  /**
   * Generator yielding the same constant value
   *
   * @param value constant value to yield
   * @param <T> type of constant value
   * @return constant generator
   */
  public static <T> Generator<T> constGen(T value) {
    return re -> value;
  }

  /**
   * Generator following the Bernoulli distribution and yielding true with probability of given rate
   * and yielding false with probability of {@code 1-trueRate}.
   *
   * @param trueRate probability of true
   * @return Bernoulli-like generator
   * @throws IllegalArgumentException if given rate is not between 0 and 1
   */
  public static Generator<Boolean> coin(double trueRate) {
    checkArgument(trueRate >= 0 && trueRate <= 1);
    return re -> re.nextDouble() <= trueRate;
  }

  /**
   * Generator selecting uniformly a element in the given universe of possibilities.
   *
   * @see #oneOf(Object[])
   *
   * @param objects universe of possibilities
   * @param <T> type of elements to pick
   * @return uniform generator of one of given universe
   * @throws IllegalArgumentException is given universe is empty
   * @throws NullPointerException if given universe is null
   */
  public static <T> Generator<T> oneOf(Collection<? extends T> objects) {
    checkArgument(!objects.isEmpty());
    return re -> Iterables.get(objects, re.nextInt(objects.size()));
  }

  /**
   * Generator selecting uniformly a element in the given universe of possibilities.
   *
   * @see #oneOf(Collection)
   *
   * @param objects universe of possibilities
   * @param <T> type of elements to pick
   * @return uniform generator of one of given universe
   * @throws IllegalArgumentException is given universe is empty
   */
  @SafeVarargs
  public static <T> Generator<T> oneOf(T... objects) {
    return oneOf(ImmutableList.copyOf(objects));
  }

  /**
   * Generator selecting uniformly a generator to execute in the given universe of possible generators.
   *
   * @see #oneGenOf(Generator[])
   *
   * @param generators universe of possible generators
   * @param <T> type of generated elements
   * @return uniform generator of one generator of given universe
   * @throws IllegalArgumentException is given universe is empty
   * @throws NullPointerException if given universe is null
   */
  public static <T> Generator<T> oneGenOf(Collection<Generator<? extends T>> generators) {
    checkArgument(!generators.isEmpty());
    return re -> Iterables.get(generators, re.nextInt(generators.size())).get(re);
  }

  /**
   * Generator selecting uniformly a generator to execute in the given universe of possible generators.
   *
   * @see #oneGenOf(Collection)
   *
   * @param generators universe of possible generators
   * @param <T> type of generated elements
   * @return uniform generator of one generator of given universe
   * @throws IllegalArgumentException is given universe is empty
   */
  @SafeVarargs
  public static <T> Generator<T> oneGenOf(Generator<? extends T>... generators) {
    return oneGenOf(ImmutableList.copyOf(generators));
  }

  /**
   * Decorating generator yielding null value at the given rate probability
   * and yielding given generator values at {@code 1 - nullRate} probability.
   *
   * @param generator delegate generator
   * @param nullRate probability of a null value
   * @param <T> type of generated elements
   * @return given generator with null values yielded
   * @throws IllegalArgumentException if given rate is not between 0 and 1
   * @throws NullPointerException if given universe is null
   */
  public static <T> Generator<T> nullable(Generator<? extends T> generator, double nullRate) {
    checkNotNull(generator);
    checkArgument(nullRate >= 0 && nullRate <= 1);
    return re -> re.nextDouble() <= nullRate ? null : generator.get(re);
  }

  /**
   * New generators selected by given boolean generator and yielding value of true generator is case of true,
   * else those of the false generator.
   *
   * @param trueGen delegate to fetch the value of in case of true
   * @param falseGen delegate to fetch the value of in case of false
   * @param boolGen selecting generator
   * @param <T> type of generated elements
   * @return selecting generator between both delegates
   * @throws NullPointerException if any of given generators are null
   */
  public static <T> Generator<T> selection(Generator<? extends T> trueGen, Generator<? extends T> falseGen, Generator<Boolean> boolGen) {
    checkNotNull(trueGen);
    checkNotNull(falseGen);
    checkNotNull(boolGen);
    return re -> boolGen.get(re) ? trueGen.get(re) : falseGen.get(re);
  }

  /**
   * Filtered generator using given generator as base and filtering all values rejected by the given predicate.
   * If the base generator yield the same filtered value {@link #MAX_FILTER_LOOP} times in a row,
   * the generator throw an exception to break the potential infinite loop.
   *
   * @param generator delegate generator
   * @param predicate filtering predicate
   * @param <T> type of generated elements
   * @return filtered generator
   * @throws NullPointerException if the generator of the predicate are null
   */
  public static <T> Generator<T> filter(Generator<? extends T> generator, Predicate<T> predicate) {
    checkNotNull(generator);
    checkNotNull(predicate);
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

  /**
   * Filtered generator using given generator as base and filtering all values rejected by the given predicate.
   * If the base generator yield the same filtered value {@link #MAX_FILTER_LOOP} times in a row,
   * the generator throw an exception to break the potential infinite loop.
   *
   * @param generator delegate generator
   * @param predicate filtering predicate
   * @return filtered generator
   * @throws NullPointerException if the generator of the predicate are null
   */
  public static Generator<Double> filter(Generator<Double> generator, DoublePredicate predicate) {
    checkNotNull(generator);
    checkNotNull(predicate);
    return re -> {
      double value;
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

  /**
   * Filtered generator using given generator as base and filtering all values rejected by the given predicate.
   * If the base generator yield the same filtered value {@link #MAX_FILTER_LOOP} times in a row,
   * the generator throw an exception to break the potential infinite loop.
   *
   * @param generator delegate generator
   * @param predicate filtering predicate
   * @return filtered generator
   * @throws NullPointerException if the generator of the predicate are null
   */
  public static Generator<Long> filter(Generator<Long> generator, LongPredicate predicate) {
    checkNotNull(generator);
    checkNotNull(predicate);
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

  /**
   * Filtered generator using given generator as base and filtering all values rejected by the given predicate.
   * If the base generator yield the same filtered value {@link #MAX_FILTER_LOOP} times in a row,
   * the generator throw an exception to break the potential infinite loop.
   *
   * @param generator delegate generator
   * @param predicate filtering predicate
   * @return filtered generator
   * @throws NullPointerException if the generator of the predicate are null
   */
  public static Generator<Integer> filter(Generator<Integer> generator, IntPredicate predicate) {
    checkNotNull(generator);
    checkNotNull(predicate);
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

  /**
   * Transformed generator using given generator as base and mapping values by the given function.
   *
   * @param generator base generator
   * @param mapper function mapping all yielded value of the generator
   * @param <T> type of elements yielded by the generator
   * @param <R> output type of mapped elements
   * @return transformed generator
   * @throws NullPointerException if the generator of the function are null
   */
  public static <T, R> Generator<R> map(Generator<? extends T> generator, Function<T, R> mapper) {
    checkNotNull(generator);
    checkNotNull(mapper);
    return re -> mapper.apply(generator.get(re));
  }

  /**
   * Transformed generator using given generator as base and mapping by the given function.
   *
   * @param generator base generator
   * @param binder function mapping all yielded value of the generator to a new generator
   * @param <T> type of elements yielded by the generator
   * @param <R> output type of mapped elements
   * @return bound generator
   * @throws NullPointerException if the generator of the function are null
   */
  public static <T, R> Generator<R> flatMap(Generator<? extends T> generator, Function<T, Generator<R>> binder) {
    checkNotNull(generator);
    checkNotNull(binder);
    return re -> binder.apply(generator.get(re)).get(re);
  }

  /**
   * Returns a new coarbitrary generator used to create functional generator of type a -> b.
   *
   * The produced generator used the {@link Object#hashCode()} of the input value to perturb the random generator.
   * With the perturbed random generator, it fetch value from output generator.
   * The random generator state is restored after each call.
   *
   * It's guarantee that equal elements (precisely with same hash code) implies a constant generated output
   * when using same Random implementation.
   *
   * @param input value
   * @param outputGen generator of output values
   * @param <T> type of output
   * @return coarbitrary generator
   */
  public static <T> Generator<T> coGenerator(Object input, Generator<T> outputGen) {
    checkNotNull(outputGen);
    int newSeed = Objects.hashCode(input);

    Function<Random, AtomicLong> seedGetter = Reflections.uncheckedFieldGetter(Random.class, "seed");
    return re -> {
      long previousSeed = seedGetter.apply(re).get();
      T out;
      try {
        re.setSeed(newSeed);
        out = outputGen.get(re);
      } finally {
        re.setSeed(previousSeed);
      }
      return out;
    };
  }

}
