package fr.pturpin.quickcheck.generator.java.math;

import fr.pturpin.quickcheck.identifier.Identifiers;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.test.configuration.DefaultRegistryFactory;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.stream.Stream;

import static fr.pturpin.quickcheck.generator.GeneratorAssertions.assertIsInRegistry;

/**
 * Created by pturpin on 18/06/2017.
 */
public class JavaMaths_UT {

  @Test
  public void bigIntegerShouldBeInDefaultRegistry() {
    assertIsInRegistries(BigInteger.class);
  }

  @Test
  public void bigDecimalShouldBeInDefaultRegistry() {
    assertIsInRegistries(BigDecimal.class);
  }

  @Test
  public void mathContextShouldBeInDefaultRegistry() {
    assertIsInRegistries(MathContext.class);
  }

  private static Stream<Registry> getRegistries() {
    return Stream.of(new DefaultRegistryFactory().create(), JavaMaths.mathRegistry());
  }

  private static <T> void assertIsInRegistries(Class<T> klass) {
    getRegistries().forEach(registry -> assertIsInRegistry(registry, klass, Identifiers::classId));
  }

}
