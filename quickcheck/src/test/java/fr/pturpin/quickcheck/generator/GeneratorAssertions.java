package fr.pturpin.quickcheck.generator;

import fr.pturpin.quickcheck.identifier.Identifiers;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.identifier.WildcardIdentifier;
import fr.pturpin.quickcheck.registry.Registry;
import org.junit.Assert;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

/**
 * Created by pturpin on 18/06/2017.
 */
public class GeneratorAssertions {

  private static final long SEED = 0L;
  private static final long NB_SAMPLES = 1000;

  public static <T> void assertNotNull(Generator<T> generator) {
    assertProperty(generator, Assert::assertNotNull);
  }

  public static <T> void assertNotNull(Stream<Generator<T>> generators) {
    generators.forEach(GeneratorAssertions::assertNotNull);
  }

  public static <T> void assertProperty(Stream<Generator<T>> generators, Consumer<T> checker) {
    generators.forEach(gen -> assertProperty(gen, checker));
  }

  public static <T> void assertProperty(Generator<T> generator, Consumer<T> checker) {
    Random random = new Random(SEED);
    for (int i = 0; i < NB_SAMPLES; i++) {
      checker.accept(generator.get(random));
    }
  }

  public static <T> void assertIsInRegistry(Registry registry, Class<T> klass, Function<Class<T>, TypeIdentifier<T>> identifierF) {
    TypeIdentifier<T> identifier = identifierF.apply(klass);
    Optional<? extends Generator<T>> generator = registry.lookup(identifier);
    Assert.assertTrue(generator.isPresent());

    Random re = new Random(0);
    for (int i = 0; i < 1000; i++) {
      T object = generator.get().get(re);
      Assert.assertNotNull(object);
      Assert.assertTrue(klass.isAssignableFrom(object.getClass()));
    }
  }

  public static <T> TypeIdentifier<T> fillIdentifier(Class<T> klass, TypeIdentifier<?> filler) {
    TypeIdentifier<T> classId = classId(klass);
    TypeIdentifier<?> filled = Identifiers.replace(id -> id instanceof WildcardIdentifier ? Optional.of(filler) : Optional.empty(), classId);
    return (TypeIdentifier) filled;
  }

}
