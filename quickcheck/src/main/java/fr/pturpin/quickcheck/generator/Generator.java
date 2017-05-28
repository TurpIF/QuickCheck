package fr.pturpin.quickcheck.generator;

import java.util.Random;

/**
 * Generator interface producing typed values given a random engine.
 *
 * @param <T> type of generated values
 */
@FunctionalInterface
public interface Generator<T> {

  /**
   * Yields a new generated value with the given random engine.
   *
   * @param re mutable random engine
   * @return randomized value
   */
  T get(Random re);
}
