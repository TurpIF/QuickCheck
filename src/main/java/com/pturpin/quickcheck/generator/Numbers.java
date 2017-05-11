package com.pturpin.quickcheck.generator;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;

public class Numbers {

  public static Generator<Integer> integerGen() {
    return integerGen(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public static Generator<Integer> integerGen(int min, int max) {
    checkArgument(max > min);
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
    return longGen(Long.MIN_VALUE, Long.MAX_VALUE);
  }

  public static Generator<Long> longGen(long min, long max) {
    checkArgument(max > min);
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
    return doubleGen(-Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public static Generator<Double> doubleGen(double min, double max) {
    checkArgument(max > min);
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

  public static Generator<BigInteger> bigIntegerGen(BigInteger min, BigInteger max) {
    checkArgument(max.compareTo(min) > 0);
    BigInteger delta = max.subtract(min);

    int deltaNumBits = delta.bitCount();
    Generator<Integer> numBitsGen = integerGen(0, deltaNumBits);

    return re -> {
      BigInteger generated = new BigInteger(numBitsGen.get(re), re);
      return generated.mod(delta).add(min);
    };
  }

  public static Generator<BigDecimal> bigDecimalGen(BigDecimal min, BigDecimal max) {
    checkArgument(max.compareTo(min) > 0);

    BigInteger minInt = min.unscaledValue();
    BigInteger maxInt = max.unscaledValue();
    Generator<BigInteger> bigIntGen = minInt.equals(maxInt)
        ? Generators.constGen(BigInteger.ZERO)
        : bigIntegerGen(minInt.min(maxInt), minInt.max(maxInt));

    int minScale = min.scale();
    int maxScale = max.scale();
    Generator<Integer> scaleGen = minScale == maxScale
        ? Generators.constGen(0)
        : integerGen(Math.min(minScale, maxScale), Math.max(minScale, maxScale));

    BigDecimal delta = max.subtract(min);

    return re -> {
      BigDecimal generated = new BigDecimal(bigIntGen.get(re), scaleGen.get(re));
      BigDecimal remainder = generated.remainder(delta);
      BigDecimal modulus = remainder.signum() >= 0 ? remainder : remainder.add(delta);
      return modulus.add(min);
    };
  }
}
