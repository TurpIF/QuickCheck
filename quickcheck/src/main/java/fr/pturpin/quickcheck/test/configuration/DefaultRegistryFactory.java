package fr.pturpin.quickcheck.test.configuration;

import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.identifier.ClassIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.generator.Numbers;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by turpif on 28/04/17.
 */
public final class DefaultRegistryFactory implements RegistryFactory {

  public DefaultRegistryFactory() {
    // nothing
  }

  @Override
  public Registry create() {
    Range<BigInteger> bigIntegerRange = Ranges.closed(
        BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.valueOf(1000)),
        BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.valueOf(1000)));

    Range<BigDecimal> bigDecimalRange = Ranges.closed(
        BigDecimal.valueOf(-Double.MAX_VALUE).multiply(BigDecimal.valueOf(1000)),
        BigDecimal.valueOf(Double.MIN_VALUE).multiply(BigDecimal.valueOf(1000)));

    return Registries.builder()
        .put(new ClassIdentifier<>(double.class), Numbers.doubleGen())
        .put(new ClassIdentifier<>(Double.class), Numbers.doubleGen())
        .put(new ClassIdentifier<>(int.class), Numbers.integerGen())
        .put(new ClassIdentifier<>(Integer.class), Numbers.integerGen())
        .put(new ClassIdentifier<>(long.class), Numbers.longGen())
        .put(new ClassIdentifier<>(Long.class), Numbers.longGen())
        .put(new ClassIdentifier<>(BigInteger.class), Numbers.bigIntegerGen(bigIntegerRange))
        .put(new ClassIdentifier<>(BigDecimal.class), Numbers.bigDecimalGen(bigDecimalRange))
        .build();
  }
}
