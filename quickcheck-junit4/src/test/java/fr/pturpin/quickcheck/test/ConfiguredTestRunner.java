package fr.pturpin.quickcheck.test;

import com.google.common.collect.ImmutableMap;
import fr.pturpin.quickcheck.functional.Checked.CheckedConsumer;
import fr.pturpin.quickcheck.generator.Numbers;
import fr.pturpin.quickcheck.identifier.ClassIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.test.configuration.RandomFactory;
import fr.pturpin.quickcheck.test.configuration.RegistryFactory;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Created by pturpin on 21/05/2017.
 */
public class ConfiguredTestRunner {

  static final class EmptyRegistryFactory implements RegistryFactory {
    public EmptyRegistryFactory() {}
    @Override  public Registry create() {
      return Registries.empty();
    }
  }

  static final class SpecialDoubleRegistryFactory implements RegistryFactory {
    public SpecialDoubleRegistryFactory() {}
    @Override  public Registry create() {
      return Registries.forMap(ImmutableMap.of(
          new ClassIdentifier<>(double.class), Numbers.specialDouble()
      ));
    }
  }

  static final class RandomSeed1Factory implements RandomFactory {
    public RandomSeed1Factory() {}
    @Override public Random create() {
      return new Random(1);
    }
  }

  static final class RandomSeed2Factory implements RandomFactory {
    public RandomSeed2Factory() {}
    @Override public Random create() {
      return new Random(2);
    }
  }

  static <T, X extends Exception> Consumer<T> unchecked(CheckedConsumer<T, X> consumer) {
    return value -> {
      try {
        consumer.accept(value);
      } catch (Exception e) {
        throw new AssertionError(e);
      }
    };
  }

  static final class State {
    long counter;
    boolean firstDoubleInitialized;
    double firstDouble;

    State() {
      this.counter = 0;
      this.firstDoubleInitialized = false;
      this.firstDouble = Double.NaN;
    }
  }

}
