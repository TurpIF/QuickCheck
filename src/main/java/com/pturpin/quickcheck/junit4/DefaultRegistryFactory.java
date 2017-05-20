package com.pturpin.quickcheck.junit4;

import com.pturpin.quickcheck.base.Ranges;
import com.pturpin.quickcheck.base.Ranges.Range;
import com.pturpin.quickcheck.identifier.ClassIdentifier;
import com.pturpin.quickcheck.registry.Registries;
import com.pturpin.quickcheck.registry.Registry;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.pturpin.quickcheck.generator.Numbers.*;

/**
 * Created by turpif on 28/04/17.
 */
final class DefaultRegistryFactory implements RandomRunner.RegistryFactory {

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
        .put(new ClassIdentifier<>(double.class), doubleGen())
        .put(new ClassIdentifier<>(Double.class), doubleGen())
        .put(new ClassIdentifier<>(int.class), integerGen())
        .put(new ClassIdentifier<>(Integer.class), integerGen())
        .put(new ClassIdentifier<>(long.class), longGen())
        .put(new ClassIdentifier<>(Long.class), longGen())
        .put(new ClassIdentifier<>(BigInteger.class), bigIntegerGen(bigIntegerRange))
        .put(new ClassIdentifier<>(BigDecimal.class), bigDecimalGen(bigDecimalRange))
        .build();
  }
}
