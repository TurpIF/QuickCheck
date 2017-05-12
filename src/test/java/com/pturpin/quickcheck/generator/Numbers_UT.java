package com.pturpin.quickcheck.generator;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Created by pturpin on 10/05/2017.
 */
public class Numbers_UT {

  private static final long SEED = 0L;
  private static final long NB_SAMPLES = 10000;
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
      int min = range.getKey();
      int max = range.getValue();
      Generator<Integer> generator = Numbers.integerGen(min, max);
      assertProperty(generator, value -> Assert.assertTrue(min <= value && value <= max));
    });
  }

  @Test
  public void boundedLongGenShouldBeBounded() {
    longRanges().forEach(range -> {
      long min = range.getKey();
      long max = range.getValue();
      Generator<Long> generator = Numbers.longGen(min, max);
      assertProperty(generator, value -> Assert.assertTrue(min <= value && value <= max));
    });
  }

  @Test
  public void boundedDoubleGenShouldBeBounded() {
    doubleRanges().forEach(range -> {
      double min = range.getKey();
      double max = range.getValue();
      Generator<Double> generator = Numbers.doubleGen(min, max);
      assertProperty(generator, value -> Assert.assertTrue(min <= value && value <= max));
    });
  }

  @Test
  public void boundedBigIntegerGenShouldBeBounded() {
    bigIntegerRanges().forEach(range -> {
      BigInteger min = range.getKey();
      BigInteger max = range.getValue();
      Generator<BigInteger> generator = Numbers.bigIntegerGen(min, max);
      assertProperty(generator, value -> Assert.assertTrue(min.compareTo(value) <= 0 && value.compareTo(max) <= 0));
    });
  }

  @Test
  public void boundedBigDecimalGenShouldBeBounded() {
    bigDecimalRanges().forEach(range -> {
      BigDecimal min = range.getKey();
      BigDecimal max = range.getValue();
      Generator<BigDecimal> generator = Numbers.bigDecimalGen(min, max);
      assertProperty(generator, value -> Assert.assertTrue(min.compareTo(value) <= 0 && value.compareTo(max) <= 0));
    });
  }

  @Test
  public void integerGenShouldThrowIfGivenReversedOrEmptyRange() {
    integerRanges().forEach(range -> {
      int min = range.getKey();
      int max = range.getValue();
      assertThrow(() -> Numbers.integerGen(max, min));
      assertThrow(() -> Numbers.integerGen(max, max));
    });
  }

  @Test
  public void longGenShouldThrowIfGivenReversedOrEmptyRange() {
    longRanges().forEach(range -> {
      long min = range.getKey();
      long max = range.getValue();
      assertThrow(() -> Numbers.longGen(max, min));
      assertThrow(() -> Numbers.longGen(max, max));
    });
  }

  @Test
  public void doubleGenShouldThrowIfGivenReversedOrEmptyRange() {
    doubleRanges().forEach(range -> {
      double min = range.getKey();
      double max = range.getValue();
      assertThrow(() -> Numbers.doubleGen(max, min));
      assertThrow(() -> Numbers.doubleGen(max, max));
    });
  }

  @Test
  public void bitIntegerGenShouldThrowIfGivenReversedOrEmptyRange() {
    bigIntegerRanges().forEach(range -> {
      BigInteger min = range.getKey();
      BigInteger max = range.getValue();
      assertThrow(() -> Numbers.bigIntegerGen(max, min));
      assertThrow(() -> Numbers.bigIntegerGen(max, max));
    });
  }

  @Test
  public void bitDecimalGenShouldThrowIfGivenReversedOrEmptyRange() {
    bigDecimalRanges().forEach(range -> {
      BigDecimal min = range.getKey();
      BigDecimal max = range.getValue();
      assertThrow(() -> Numbers.bigDecimalGen(max, min));
      assertThrow(() -> Numbers.bigDecimalGen(max, max));
    });
  }

  @Test
  public void doubleGenShouldThrowIfGivenNonFiniteBound() {
    Arrays.asList(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).forEach(bound -> {
      assertThrow(() -> Numbers.doubleGen(0.d, bound));
      assertThrow(() -> Numbers.doubleGen(bound, 0.d));
    });
  }

  @Test
  public void bitIntegerGenShouldThrowIfGivenNullBound() {
    assertThrow(() -> Numbers.bigIntegerGen(null, BigInteger.ONE));
    assertThrow(() -> Numbers.bigIntegerGen(BigInteger.ONE, null));
  }

  @Test
  public void bitDecimalGenShouldThrowIfGivenNullBound() {
    assertThrow(() -> Numbers.bigDecimalGen(null, BigDecimal.ONE));
    assertThrow(() -> Numbers.bigDecimalGen(BigDecimal.ONE, null));
  }

  private static Stream<Map.Entry<Integer, Integer>> integerRanges() {
    Random random = new Random(SEED);

    int[] bounds = IntStream.concat(
        IntStream.of(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1),
        random.ints())
        .limit(NB_BOUNDS)
        .toArray();

    return Arrays.stream(bounds)
        .boxed()
        .flatMap(bound1 -> Arrays.stream(bounds)
            .filter(bound2 -> bound2 > bound1)
            .boxed()
            .map(bound2 -> Maps.immutableEntry(bound1, bound2)));
  }

  private static Stream<Map.Entry<Long, Long>> longRanges() {
    Random random = new Random(SEED);

    long[] bounds = LongStream.concat(
        LongStream.of(Long.MIN_VALUE, Long.MAX_VALUE, 0, 1),
        random.longs())
        .limit(NB_BOUNDS)
        .toArray();

    return Arrays.stream(bounds)
        .boxed()
        .flatMap(bound1 -> Arrays.stream(bounds)
            .filter(bound2 -> bound2 > bound1)
            .boxed()
            .map(bound2 -> Maps.immutableEntry(bound1, bound2)));
  }

  private static Stream<Map.Entry<Double, Double>> doubleRanges() {
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
            .filter(bound2 -> bound2 > bound1)
            .boxed()
            .map(bound2 -> Maps.immutableEntry(bound1, bound2)));
  }

  private static Stream<Map.Entry<BigInteger, BigInteger>> bigIntegerRanges() {
    return longRanges().map(range -> Maps.immutableEntry(BigInteger.valueOf(range.getKey()), BigInteger.valueOf(range.getValue())));
  }

  private static Stream<Map.Entry<BigDecimal, BigDecimal>> bigDecimalRanges() {
    return doubleRanges().map(range -> Maps.immutableEntry(BigDecimal.valueOf(range.getKey()), BigDecimal.valueOf(range.getValue())));
  }

  private static Stream<Generator<Integer>> integerGens() {
    return Stream.concat(Stream.of(Numbers.integerGen()),
        integerRanges().map(range -> Numbers.integerGen(range.getKey(), range.getValue())));
  }

  private static Stream<Generator<Long>> longGens() {
    return Stream.concat(Stream.of(Numbers.longGen()),
        longRanges().map(range -> Numbers.longGen(range.getKey(), range.getValue())));
  }

  private static Stream<Generator<Double>> doubleGens() {
    return Stream.concat(Stream.of(Numbers.doubleGen()),
        doubleRanges().map(range -> Numbers.doubleGen(range.getKey(), range.getValue())));
  }

  private static Stream<Generator<BigInteger>> bigIntegerGens() {
    return bigIntegerRanges().map(range -> Numbers.bigIntegerGen(range.getKey(), range.getValue()));
  }

  private static Stream<Generator<BigDecimal>> bigDecimalGens() {
    return bigDecimalRanges().map(range -> Numbers.bigDecimalGen(range.getKey(), range.getValue()));
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

  private static void assertThrow(CheckedSupplier<?, ?> supplier) {
    try {
      supplier.get();
      Assert.fail();
    } catch (Exception ignored) {
    }
  }

  public interface CheckedSupplier<T, X extends Exception> {
    T get() throws X;
  }
}
