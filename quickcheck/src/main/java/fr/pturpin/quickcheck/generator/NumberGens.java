package fr.pturpin.quickcheck.generator;

import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.DoubleRange;
import fr.pturpin.quickcheck.base.Ranges.IntRange;
import fr.pturpin.quickcheck.base.Ranges.LongRange;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.isFinite;

public final class NumberGens {

  private NumberGens() {
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
}
