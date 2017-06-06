package fr.pturpin.quickcheck.generator;

import com.google.common.collect.ImmutableSet;
import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.DoubleRange;
import fr.pturpin.quickcheck.base.Ranges.IntRange;
import fr.pturpin.quickcheck.base.Ranges.LongRange;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.assertion.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Created by pturpin on 10/05/2017.
 */
public class NumberGens_UT {

  private static final long SEED = 0L;
  private static final long NB_SAMPLES = 1000;
  private static final long NB_BOUNDS = 20;

  @Test
  public void integerGenShouldNotGenerateNull() {
    assertNotNull(integerGens());
  }

  @Test
  public void longGenShouldNotGenerateNull() {
    assertNotNull(longGens());
  }

  @Test
  public void doubleGenShouldNotGenerateNull() {
    assertNotNull(doubleGens());
  }

  @Test
  public void specialDoubleGenShouldNotGenerateNull() {
    assertNotNull(NumberGens.specialDouble());
  }

  @Test
  public void bigIntegerGenShouldNotGenerateNull() {
    assertNotNull(bigIntegerGens());
  }

  @Test
  public void bigDecimalGenShouldNotGenerateNull() {
    assertNotNull(bigDecimalGens());
  }

  @Test
  public void doubleGenShouldNotGenerateNaNNorInfinity() {
    assertProperty(doubleGens(), value -> Assert.assertTrue(Double.isFinite(value)));
  }

  @Test
  public void boundedIntegerGenShouldBeBounded() {
    integerRanges().forEach(range -> {
      Generator<Integer> generator = NumberGens.integerGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedLongGenShouldBeBounded() {
    longRanges().forEach(range -> {
      Generator<Long> generator = NumberGens.longGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedDoubleGenShouldBeBounded() {
    doubleRanges().forEach(range -> {
      Generator<Double> generator = NumberGens.doubleGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedBigIntegerGenShouldBeBounded() {
    bigIntegerRanges().forEach(range -> {
      Generator<BigInteger> generator = NumberGens.bigIntegerGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedBigDecimalGenShouldBeBounded() {
    bigDecimalRanges().forEach(range -> {
      Generator<BigDecimal> generator = NumberGens.bigDecimalGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void doubleGenShouldThrowIfGivenNonFiniteBound() {
    Arrays.asList(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).forEach(bound -> {
      Assertions.assertThrow(() -> NumberGens.doubleGen(Ranges.closed(0.d, bound.doubleValue())));
      Assertions.assertThrow(() -> NumberGens.doubleGen(Ranges.closed(bound.doubleValue(), 0.d)));
    });
  }

  @Test
  public void bigIntegerGenShouldThrowIfGivenNullRange() {
    Assertions.assertThrow(() -> NumberGens.bigIntegerGen(null));
  }

  @Test
  public void bigDecimalGenShouldThrowIfGivenNullRange() {
    Assertions.assertThrow(() -> NumberGens.bigDecimalGen(null));
  }

  private static Stream<IntRange> integerRanges() {
    Random random = new Random(SEED);

    int[] bounds = IntStream.concat(
        IntStream.of(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1),
        random.ints())
        .flatMap(bound -> IntStream.of(-bound, bound))
        .distinct()
        .limit(NB_BOUNDS)
        .toArray();

    return Arrays.stream(bounds)
        .boxed()
        .flatMap(bound1 -> Arrays.stream(bounds)
            .filter(bound2 -> bound2 >= bound1)
            .boxed()
            .flatMap(bound2 -> Stream.<IntRange>of(
                Ranges.closed(bound1.intValue(), bound2.intValue()),
                Ranges.opened(bound1.intValue(), bound2.intValue())
            )))
        .filter(range -> !range.isEmpty());
  }

  private static Stream<LongRange> longRanges() {
    Random random = new Random(SEED);

    long[] bounds = LongStream.concat(
        LongStream.of(Long.MIN_VALUE, Long.MAX_VALUE, 0, 1),
        random.longs())
        .flatMap(bound -> LongStream.of(-bound, bound))
        .distinct()
        .limit(NB_BOUNDS)
        .toArray();

    return Arrays.stream(bounds)
        .boxed()
        .flatMap(bound1 -> Arrays.stream(bounds)
            .filter(bound2 -> bound2 >= bound1)
            .boxed()
            .flatMap(bound2 -> Stream.<LongRange>of(
                Ranges.closed(bound1.longValue(), bound2.longValue()),
                Ranges.opened(bound1.longValue(), bound2.longValue()))))
        .filter(range -> !range.isEmpty());
  }

  private static Stream<DoubleRange> doubleRanges() {
    Random random = new Random(SEED);

    double[] bounds = DoubleStream.concat(
        DoubleStream.of(Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, 0.d, 1.d),
        random.doubles().map(value -> value * Double.MAX_VALUE * (random.nextBoolean() ? 1 : -1)))
        .flatMap(bound -> DoubleStream.of(-bound, bound))
        .distinct()
        .limit(NB_BOUNDS)
        .toArray();

    return Arrays.stream(bounds)
        .boxed()
        .flatMap(bound1 -> Arrays.stream(bounds)
            .filter(bound2 -> bound2 >= bound1)
            .boxed()
            .flatMap(bound2 -> Stream.<DoubleRange>of(
                Ranges.closed(bound1.doubleValue(), bound2.doubleValue()),
                Ranges.opened(bound1.doubleValue(), bound2.doubleValue())
            )))
        .filter(range -> !range.isEmpty());
  }

  private static Stream<Range<BigInteger>> bigIntegerRanges() {
    ImmutableSet<BigInteger> multipliers = ImmutableSet.of(BigInteger.ONE, BigInteger.TEN, BigInteger.valueOf(1234));

    return longRanges().map(range -> Ranges.map(Ranges.boxed(range), BigInteger::valueOf))
        .flatMap(range -> multipliers.stream()
            .map(multiplier -> Ranges.map(range, (BigInteger bound) -> bound.multiply(multiplier))));
  }

  private static Stream<Range<BigDecimal>> bigDecimalRanges() {
    ImmutableSet<BigDecimal> multipliers = ImmutableSet.of(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(1234),
        BigDecimal.valueOf(1e-1), BigDecimal.valueOf(1e-5), BigDecimal.valueOf(1e-12));

    return doubleRanges().map(range -> Ranges.map(Ranges.boxed(range), BigDecimal::valueOf))
        .flatMap(range -> multipliers.stream()
            .map(multiplier -> Ranges.map(range, (BigDecimal bound) -> bound.multiply(multiplier))));
  }

  private static Stream<Generator<Integer>> integerGens() {
    return Stream.concat(Stream.of(NumberGens.integerGen()),
        integerRanges().map(NumberGens::integerGen));
  }

  private static Stream<Generator<Long>> longGens() {
    return Stream.concat(Stream.of(NumberGens.longGen()),
        longRanges().map(NumberGens::longGen));
  }

  private static Stream<Generator<Double>> doubleGens() {
    return Stream.concat(Stream.of(NumberGens.doubleGen()),
        doubleRanges().map(NumberGens::doubleGen));
  }

  private static Stream<Generator<BigInteger>> bigIntegerGens() {
    return bigIntegerRanges().map(NumberGens::bigIntegerGen);
  }

  private static Stream<Generator<BigDecimal>> bigDecimalGens() {
    return bigDecimalRanges().map(NumberGens::bigDecimalGen);
  }

  private static <T> void assertNotNull(Generator<T> generator) {
    assertProperty(generator, Assert::assertNotNull);
  }

  private static <T> void assertNotNull(Stream<Generator<T>> generators) {
    generators.forEach(NumberGens_UT::assertNotNull);
  }

  private static <T> void assertProperty(Stream<Generator<T>> generators, Consumer<T> checker) {
    generators.forEach(gen -> assertProperty(gen, checker));
  }

  private static <T> void assertProperty(Generator<T> generator, Consumer<T> checker) {
    Random random = new Random(SEED);
    for (int i = 0; i < NB_SAMPLES; i++) {
      checker.accept(generator.get(random));
    }
  }
}
