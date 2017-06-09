package fr.pturpin.quickcheck.test.configuration;

import com.google.common.collect.ImmutableList;
import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.generator.collection.ListGens;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;
import static fr.pturpin.quickcheck.registry.Registries.DynamicRegistry.resolved;

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

    Generator<Integer> sizeGen = NumberGens.integerGen(Ranges.closed(0, 100));

    return Registries.builder()
        .put(classId(double.class), NumberGens.doubleGen())
        .put(classId(int.class), NumberGens.integerGen())
        .put(classId(long.class), NumberGens.longGen())
        .put(classId(BigInteger.class), NumberGens.bigIntegerGen(bigIntegerRange))
        .put(classId(BigDecimal.class), NumberGens.bigDecimalGen(bigDecimalRange))
        .putDyn(ImmutableList.class, resolved(gen -> ListGens.immutableListGen(gen, sizeGen)))
        .putDyn(List.class, resolved(gen -> ListGens.arrayListGen(gen, sizeGen)))
        .putDyn(Supplier.class, resolved(gen -> Generators.<Object, Supplier>map(gen, v -> () -> v)))
        .build();
  }
}
