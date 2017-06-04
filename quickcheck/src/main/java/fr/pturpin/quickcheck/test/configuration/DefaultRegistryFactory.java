package fr.pturpin.quickcheck.test.configuration;

import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.generator.collection.ListGens;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static fr.pturpin.quickcheck.identifier.ClassIdentifier.classId;
import static fr.pturpin.quickcheck.identifier.ParametrizedIdentifier.paramId;

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
        .put(classId(double.class), NumberGens.doubleGen())
        .put(classId(Double.class), NumberGens.doubleGen())
        .put(classId(int.class), NumberGens.integerGen())
        .put(classId(Integer.class), NumberGens.integerGen())
        .put(classId(long.class), NumberGens.longGen())
        .put(classId(Long.class), NumberGens.longGen())
        .put(classId(BigInteger.class), NumberGens.bigIntegerGen(bigIntegerRange))
        .put(classId(BigDecimal.class), NumberGens.bigDecimalGen(bigDecimalRange))
        .put(paramId((Class<List<Double>>) (Class) List.class, Double.class), (Registry registry) ->
            registry.lookup(classId(Double.class)).map(doubleGen -> {
              Generator<Integer> sizeGen = NumberGens.integerGen(Ranges.closed(0, 100));
              return ListGens.arrayListGen(doubleGen, sizeGen);
            }))
        .build();
  }
}
