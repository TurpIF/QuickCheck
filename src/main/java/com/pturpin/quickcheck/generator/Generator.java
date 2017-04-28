package com.pturpin.quickcheck.generator;

import java.util.Random;

@FunctionalInterface
public interface Generator<T> {
  T get(Random re);
}
