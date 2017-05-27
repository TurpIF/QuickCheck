package com.pturpin.quickcheck.generator;

import com.google.common.base.Preconditions;
import com.pturpin.quickcheck.base.Ranges;
import com.pturpin.quickcheck.base.Ranges.DoubleRange;
import com.pturpin.quickcheck.base.Ranges.IntRange;
import com.pturpin.quickcheck.base.Ranges.LongRange;
import com.pturpin.quickcheck.base.Ranges.Range;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.isFinite;

public final class Numbers {

  private Numbers() {
    /* factory class */
  }

  /**
   * Returns a new uniform integer generator bounded between
   * {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE} included.
   *
   * @return uniform integer generator
   */
  public static Generator<Integer> integerGen() {
    return integerGen(Ranges.closed(Integer.MIN_VALUE, Integer.MAX_VALUE));
  }

  /**
   * Constructs a new uniform integer generators bounded by given range.
   * The distance between bounds of range can be higher than
   * {@link Integer#MAX_VALUE}.
   *
   * @param range range to bound the generated value
   * @return bounded uniform integer generators
   * @throws IllegalArgumentException if given range is empty
   * @throws NullPointerException if given range is null
   */
  public static Generator<Integer> integerGen(IntRange range) {
    checkArgument(!range.isEmpty());
    int min = range.getLeft() + (range.isLeftClosed() ? 0 : 1);
    int max = range.getRight() - (range.isRightClosed() ? 0 : 1);

    if (min == max) {
      return Generators.constGen(min);
    } else if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
      return Random::nextInt;
    } else if (min == -max) {
      if (max == Integer.MAX_VALUE) {
        return Generators.filter(Random::nextInt, (int value) -> value != Integer.MIN_VALUE);
      }
      return re -> re.nextInt(max + 1) * (re.nextBoolean() ? 1 : -1);
    }

    // no overflow see Math#subtractExact
    int delta = max - min;
    if (((max ^ min) & (max ^ delta)) >= 0 && delta != Integer.MAX_VALUE) {
      return re -> re.nextInt(delta + 1) + min;
    }

    // Use long to avoid overflow
    long lMin = min;
    long lMax = max;
    long lDeltaPlusOne = lMax - lMin + 1;
    return re -> (int) (Math.floorMod((long) re.nextInt(), lDeltaPlusOne) + lMin);
  }

  /**
   * Returns a new uniform long generator bounded between
   * {@link Long#MIN_VALUE} and {@link Long#MAX_VALUE} included.
   *
   * @return uniform long generator
   */
  public static Generator<Long> longGen() {
    return longGen(Ranges.closed(Long.MIN_VALUE, Long.MAX_VALUE));
  }

  /**
   * Constructs a new uniform long generators bounded by given range.
   * The distance between bounds of range can be higher than
   * {@link Long#MAX_VALUE}.
   *
   * @param range range to bound the generated value
   * @return bounded uniform long generators
   * @throws IllegalArgumentException if given range is empty
   * @throws NullPointerException if given range is null
   */
  public static Generator<Long> longGen(LongRange range) {
    checkArgument(!range.isEmpty());
    long min = range.getLeft() + (range.isLeftClosed() ? 0 : 1);
    long max = range.getRight() - (range.isRightClosed() ? 0 : 1);

    if (min == max) {
      return Generators.constGen(min);
    } else if (min == Long.MIN_VALUE && max == Long.MAX_VALUE) {
      return Random::nextLong;
    } else if (min == -max) {
      if (max == Integer.MAX_VALUE) {
        return Generators.filter(Random::nextLong, (long value) -> value != Long.MIN_VALUE);
      }
      return re -> Math.floorMod(re.nextLong(), max + 1) * (re.nextBoolean() ? 1 : -1);
    }

    // no overflow see Math#subtractExact
    long delta = max - min;
    if (((max ^ min) & (max ^ delta)) >= 0 && delta != Long.MAX_VALUE) {
      return re -> Math.floorMod(re.nextLong(), delta + 1) + min;
    }

    // Use BigInteger to avoid any overflow
    BigInteger bMax = BigInteger.valueOf(max);
    BigInteger bMin = BigInteger.valueOf(min);
    BigInteger bDeltaPlusOne = bMax.subtract(bMin).add(BigInteger.ONE);
    return re -> BigInteger.valueOf(re.nextLong()).mod(bDeltaPlusOne).add(bMin).longValueExact();
  }

  /**
   * Returns a new uniform double generator bounded between
   * -{@link Double#MAX_VALUE} and {@link Double#MAX_VALUE} included.
   *
   * @return uniform double generator
   */
  public static Generator<Double> doubleGen() {
    return doubleGen(Ranges.closed(-Double.MAX_VALUE, Double.MAX_VALUE));
  }

  /**
   * Constructs a new uniform double generators bounded by given range.
   * The distance between bounds of range can be higher than
   * {@link Double#MAX_VALUE}.
   *
   * @param range range to bound the generated value
   * @return bounded uniform double generators
   * @throws IllegalArgumentException if given range is empty
   * @throws NullPointerException if given range is null
   */
  public static Generator<Double> doubleGen(DoubleRange range) {
    checkArgument(!range.isEmpty());
    double min = range.isLeftClosed() ? range.getLeft() : range.getLeft() + Double.MIN_VALUE;
    double max = range.isRightClosed() ?  range.getRight() : range.getRight() - Double.MIN_VALUE;

    checkArgument(isFinite(min));
    checkArgument(isFinite(max));

    if (min == -max) {
      return re -> re.nextDouble() * max * (re.nextBoolean() ? 1 : -1);
    }

    double delta = max - min;
    if (delta != Double.POSITIVE_INFINITY) {
      return re -> re.nextDouble() * delta + min;
    }

    // Use BigDecimal to avoid overflow
    BigDecimal bMax = BigDecimal.valueOf(max);
    BigDecimal bMin = BigDecimal.valueOf(min);
    BigDecimal bDelta = bMax.subtract(bMin);
    return re -> BigDecimal.valueOf(re.nextDouble()).multiply(bDelta).add(bMin).doubleValue();
  }

  /**
   * Returns a new double generator producing special double values as NaN,
   * infinities, max/min values, min normal and pos/neg zeros.
   *
   * @return special double values generator
   */
  public static Generator<Double> specialDouble() {
    return Generators.oneOf(Double.MIN_VALUE, Double.MAX_VALUE,
        Double.MIN_NORMAL, Double.NaN, 0.d, -0.d,
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
  }

  /**
   * Constructs a new big integer double generators bounded by given range.
   *
   * @param range range to bound the generated value
   * @return bounded uniform big integer generators
   * @throws IllegalArgumentException if given range is empty
   * @throws NullPointerException if given range is null
   */
  public static Generator<BigInteger> bigIntegerGen(Range<BigInteger> range) {
    Preconditions.checkArgument(!range.isEmpty());

    if (!range.isLeftClosed() || !range.isRightClosed()) {
      Predicate<BigInteger> leftPredicate = !range.isLeftClosed()
          ? value -> range.getLeft().compareTo(value) != 0
          : value -> true;
      Predicate<BigInteger> rightPredicate = !range.isRightClosed()
          ? value -> range.getRight().compareTo(value) != 0
          : value -> true;

      return Generators.filter(bigIntegerGen(Ranges.closed(range.getLeft(), range.getRight())), leftPredicate.and(rightPredicate));
    }

    BigInteger min = range.getLeft();
    BigInteger max = range.getRight();

    if (min.compareTo(max) == 0) {
      return Generators.constGen(max);
    } else if (max.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0
        && min.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0) {
      return Generators.map(longGen(Ranges.closed(min.longValue(), max.longValue())), BigInteger::valueOf);
    }

    BigInteger delta = max.subtract(min);
    int deltaNumBits = delta.bitCount();
    Generator<Integer> numBitsGen = integerGen(Ranges.closed(0, deltaNumBits));

    return re -> {
      BigInteger generated = new BigInteger(numBitsGen.get(re), re);
      return generated.mod(delta).add(min);
    };
  }

  /**
   * Constructs a new uniform big decimal generators bounded by given range.
   *
   * @param range range to bound the generated value
   * @return bounded uniform big decimal generators
   * @throws IllegalArgumentException if given range is empty
   * @throws NullPointerException if given range is null
   */
  public static Generator<BigDecimal> bigDecimalGen(Range<BigDecimal> range) {
    checkArgument(!range.isEmpty());

    if (!range.isLeftClosed() || !range.isRightClosed()) {
      Predicate<BigDecimal> leftPredicate = !range.isLeftClosed()
          ? value -> range.getLeft().compareTo(value) != 0
          : value -> true;
      Predicate<BigDecimal> rightPredicate = !range.isRightClosed()
          ? value -> range.getRight().compareTo(value) != 0
          : value -> true;

      return Generators.filter(bigDecimalGen(Ranges.closed(range.getLeft(), range.getRight())), leftPredicate.and(rightPredicate));
    }

    BigDecimal min = range.getLeft();
    BigDecimal max = range.getRight();

    if (min.compareTo(max) == 0) {
      return Generators.constGen(min);
    }

    BigInteger minInt = min.unscaledValue();
    BigInteger maxInt = max.unscaledValue();
    Generator<BigInteger> bigIntGen = bigIntegerGen(Ranges.closed(minInt.min(maxInt), minInt.max(maxInt)));

    int minScale = min.scale();
    int maxScale = max.scale();
    Generator<Integer> scaleGen = minScale == maxScale
        ? Generators.constGen(minScale)
        : integerGen(Ranges.closed(Math.min(minScale, maxScale), Math.max(minScale, maxScale)));

    BigDecimal delta = max.subtract(min);

    return re -> {
      BigDecimal generated = new BigDecimal(bigIntGen.get(re), scaleGen.get(re));
      BigDecimal remainder = generated.remainder(delta);
      BigDecimal modulus = remainder.signum() >= 0 ? remainder : remainder.add(delta);
      return modulus.add(min);
    };
  }
}
