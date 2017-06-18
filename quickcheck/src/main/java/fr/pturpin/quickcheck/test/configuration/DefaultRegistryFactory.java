package fr.pturpin.quickcheck.test.configuration;

import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.generator.java.util.JavaUtils;
import fr.pturpin.quickcheck.generator.java.util.function.FunctionGen;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import java.math.BigDecimal;
import java.math.BigInteger;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

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

    Registry base = Registries.builder()
        .put(classId(double.class), NumberGens.doubleGen())
        .put(classId(int.class), NumberGens.integerGen(Ranges.closed(0, 50))) // FIXME handle collection size correctly
        .put(classId(long.class), NumberGens.longGen())
        .put(classId(BigInteger.class), NumberGens.bigIntegerGen(bigIntegerRange))
        .put(classId(BigDecimal.class), NumberGens.bigDecimalGen(bigDecimalRange))
        .put(classId(boolean.class), Generators.coin(0.5))
        .build();

    return Registries.alternatives(
        base,
        JavaUtils.utilsRegistry(),
        FunctionGen.functionsRegistry());
  }

}
