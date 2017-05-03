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
    return re -> re.nextInt() % (max - min) + min;
  }

  public static Generator<Long> longGen() {
    return longGen(Long.MIN_VALUE, Long.MAX_VALUE);
  }

  public static Generator<Long> longGen(long min, long max) {
    checkArgument(max > min);
    return re -> re.nextLong() % (max - min) + min;
  }

  public static Generator<Double> doubleGen() {
    return doubleGen(-Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public static Generator<Double> doubleGen(double min, double max) {
    checkArgument(max > min);
    return re -> re.nextDouble() * (max - min) + min;  // FIXME handle overflow
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
      return generated.remainder(delta).add(min);
    };
  }

  public static Generator<BigDecimal> bigDecimalGen(BigDecimal min, BigDecimal max) {
    checkArgument(max.compareTo(min) > 0);

    BigInteger minInt = min.unscaledValue();
    BigInteger maxInt = max.unscaledValue();
    Generator<BigInteger> bigIntGen = bigIntegerGen(minInt, maxInt);

    int minScale = min.scale();
    int maxScale = max.scale();
    Generator<Integer> scaleGen = integerGen(minScale, maxScale);

    BigDecimal delta = max.subtract(min);

    return re -> {
      BigDecimal generated = new BigDecimal(bigIntGen.get(re), scaleGen.get(re));
      return generated.remainder(delta).add(min);
    };
  }
}
