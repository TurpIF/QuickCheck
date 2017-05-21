package com.pturpin.quickcheck.generator;

import com.pturpin.quickcheck.base.Ranges;
import com.pturpin.quickcheck.base.Ranges.DoubleRange;
import com.pturpin.quickcheck.base.Ranges.IntRange;
import com.pturpin.quickcheck.base.Ranges.LongRange;
import com.pturpin.quickcheck.base.Ranges.Range;
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

import static com.pturpin.quickcheck.assertion.Assertions.assertThrow;

/**
 * Created by pturpin on 10/05/2017.
 */
public class Numbers_UT {

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
    assertNotNull(Numbers.specialDouble());
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
      Generator<Integer> generator = Numbers.integerGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedLongGenShouldBeBounded() {
    longRanges().forEach(range -> {
      Generator<Long> generator = Numbers.longGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedDoubleGenShouldBeBounded() {
    doubleRanges().forEach(range -> {
      Generator<Double> generator = Numbers.doubleGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedBigIntegerGenShouldBeBounded() {
    bigIntegerRanges().forEach(range -> {
      Generator<BigInteger> generator = Numbers.bigIntegerGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void boundedBigDecimalGenShouldBeBounded() {
    bigDecimalRanges().forEach(range -> {
      Generator<BigDecimal> generator = Numbers.bigDecimalGen(range);
      assertProperty(generator, value -> Assert.assertTrue(range.contains(value)));
    });
  }

  @Test
  public void doubleGenShouldThrowIfGivenNonFiniteBound() {
    Arrays.asList(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).forEach(bound -> {
      assertThrow(() -> Numbers.doubleGen(Ranges.closed(0.d, bound.doubleValue())));
      assertThrow(() -> Numbers.doubleGen(Ranges.closed(bound.doubleValue(), 0.d)));
    });
  }

  @Test
  public void bitIntegerGenShouldThrowIfGivenNullRange() {
    assertThrow(() -> Numbers.bigIntegerGen(null));
  }

  @Test
  public void bitDecimalGenShouldThrowIfGivenNullRange() {
    assertThrow(() -> Numbers.bigDecimalGen(null));
  }

  private static Stream<IntRange> integerRanges() {
    Random random = new Random(SEED);

    int[] bounds = IntStream.concat(
        IntStream.of(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1),
        random.ints())
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
        .limit(NB_BOUNDS)
        .toArray();

    return Arrays.stream(bounds)
        .boxed()
        .flatMap(bound1 -> Arrays.stream(bounds)
            .filter(bound2 -> bound2 >= bound1)
            .boxed()
            .flatMap(bound2 -> Stream.<LongRange>of(
                Ranges.closed(bound1.longValue(), bound2.longValue()),
                Ranges.opened(bound1.longValue(), bound2.longValue())
            )))
        .filter(range -> !range.isEmpty());
  }

  private static Stream<DoubleRange> doubleRanges() {
    Random random = new Random(SEED);

    double[] bounds = DoubleStream.concat(
        DoubleStream.of(Double.MAX_VALUE, -Double.MAX_VALUE,
            Double.MIN_VALUE, -Double.MIN_VALUE,
            Double.MIN_NORMAL, -Double.MIN_NORMAL,
            0.d, -0.d,
            1.d, -1.d),
        random.doubles().map(value -> value * Double.MAX_VALUE * (random.nextBoolean() ? 1 : -1)))
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
    return longRanges().map(range -> Ranges.map(Ranges.boxed(range), BigInteger::valueOf));
  }

  private static Stream<Range<BigDecimal>> bigDecimalRanges() {
    return doubleRanges().map(range -> Ranges.map(Ranges.boxed(range), BigDecimal::valueOf));
  }

  private static Stream<Generator<Integer>> integerGens() {
    return Stream.concat(Stream.of(Numbers.integerGen()),
        integerRanges().map(Numbers::integerGen));
  }

  private static Stream<Generator<Long>> longGens() {
    return Stream.concat(Stream.of(Numbers.longGen()),
        longRanges().map(Numbers::longGen));
  }

  private static Stream<Generator<Double>> doubleGens() {
    return Stream.concat(Stream.of(Numbers.doubleGen()),
        doubleRanges().map(Numbers::doubleGen));
  }

  private static Stream<Generator<BigInteger>> bigIntegerGens() {
    return bigIntegerRanges().map(Numbers::bigIntegerGen);
  }

  private static Stream<Generator<BigDecimal>> bigDecimalGens() {
    return bigDecimalRanges().map(Numbers::bigDecimalGen);
  }

  private static <T> void assertNotNull(Generator<T> generator) {
    assertProperty(generator, Assert::assertNotNull);
  }

  private static <T> void assertNotNull(Stream<Generator<T>> generators) {
    generators.forEach(Numbers_UT::assertNotNull);
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
