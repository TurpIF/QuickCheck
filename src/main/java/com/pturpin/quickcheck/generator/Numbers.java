package com.pturpin.quickcheck.generator;

import com.google.common.base.Preconditions;
import com.pturpin.quickcheck.base.Ranges;
import com.pturpin.quickcheck.base.Ranges.DoubleRange;
import com.pturpin.quickcheck.base.Ranges.IntRange;
import com.pturpin.quickcheck.base.Ranges.LongRange;
import com.pturpin.quickcheck.base.Ranges.Range;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.isFinite;

public final class Numbers {

  private Numbers() {
    /* factory class */
  }

  public static Generator<Integer> integerGen() {
    return integerGen(Ranges.closed(Integer.MIN_VALUE, Integer.MAX_VALUE));
  }

  public static Generator<Integer> integerGen(IntRange range) {
    checkArgument(!range.isEmpty());
    int min = range.getLeft() + (range.isLeftClosed() ? 0 : 1);
    int max = range.getRight() - (range.isRightClosed() ? 0 : 1);

    if (min == max) {
      return Generators.constGen(min);
    }

    return re -> {
      int delta = max - min;

      // no overflow see Math#subtractExact
      if (((max ^ min) & (max ^ delta)) >= 0 && delta != Integer.MAX_VALUE) {
        return re.nextInt(delta + 1) + min;
      }

      // Use long to avoid overflow
      long lMin = min;
      long lMax = max;
      return (int) (Math.floorMod((long) re.nextInt(), lMax - lMin) + lMin);
    };
  }

  public static Generator<Long> longGen() {
    return longGen(Ranges.closed(Long.MIN_VALUE, Long.MAX_VALUE));
  }

  public static Generator<Long> longGen(LongRange range) {
    checkArgument(!range.isEmpty());
    long min = range.getLeft() + (range.isLeftClosed() ? 0 : 1);
    long max = range.getRight() - (range.isRightClosed() ? 0 : 1);

    if (min == max) {
      return Generators.constGen(min);
    }

    return re -> {
      long delta = max - min;

      // no overflow see Math#subtractExact
      if (((max ^ min) & (max ^ delta)) >= 0 && delta != Long.MAX_VALUE) {
        return Math.floorMod(re.nextLong(), delta + 1) + min;
      }

      // Use BigInteger to avoid any overflow
      BigInteger bMax = BigInteger.valueOf(max);
      BigInteger bMin = BigInteger.valueOf(min);
      BigInteger value = BigInteger.valueOf(re.nextLong());

      return value.mod(bMax.subtract(bMin).add(BigInteger.ONE)).add(bMin).longValueExact();
    };
  }

  public static Generator<Double> doubleGen() {
    return doubleGen(Ranges.closed(-Double.MAX_VALUE, Double.MAX_VALUE));
  }

  public static Generator<Double> doubleGen(DoubleRange range) {
    checkArgument(!range.isEmpty());
    double min = range.isLeftClosed() ? range.getLeft() : range.getLeft() + Double.MIN_VALUE;
    double max = range.isRightClosed() ?  range.getRight() : range.getRight() - Double.MIN_VALUE;

    checkArgument(isFinite(min));
    checkArgument(isFinite(max));

    return re -> {
      double delta = max - min;
      if (delta != Double.POSITIVE_INFINITY) {
        return re.nextDouble() * delta + min;
      }

      // Use BigDecimal to avoid overflow
      BigDecimal bMax = BigDecimal.valueOf(max);
      BigDecimal bMin = BigDecimal.valueOf(min);
      BigDecimal value = BigDecimal.valueOf(re.nextDouble());

      return value.multiply(bMax.subtract(bMin)).add(bMin).doubleValue();
    };
  }

  public static Generator<Double> specialDouble() {
    return Generators.oneOf(Double.MIN_VALUE, Double.MAX_VALUE,
        Double.MIN_NORMAL, Double.NaN, 0.d, -0.d,
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
  }

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
    }

    BigInteger delta = max.subtract(min);
    int deltaNumBits = delta.bitCount();
    Generator<Integer> numBitsGen = integerGen(Ranges.closed(0, deltaNumBits));

    return re -> {
      BigInteger generated = new BigInteger(numBitsGen.get(re), re);
      return generated.mod(delta).add(min);
    };
  }

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
        ? Generators.constGen(1)
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
