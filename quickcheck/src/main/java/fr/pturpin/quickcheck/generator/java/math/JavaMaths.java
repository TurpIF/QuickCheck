package fr.pturpin.quickcheck.generator.java.math;

import com.google.common.base.Preconditions;
import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static fr.pturpin.quickcheck.generator.NumberGens.integerGen;
import static fr.pturpin.quickcheck.generator.NumberGens.longGen;

/**
 * Created by pturpin on 18/06/2017.
 */
public final class JavaMaths {

  private static Range<BigInteger> DEFAULT_BIG_INTEGER_RANGE = Ranges.closed(
      BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.valueOf(100)),
      BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(100)));

  private static Range<BigDecimal> DEFAULT_BIG_DECIMAL_RANGE = Ranges.closed(
      BigDecimal.valueOf(-Double.MAX_VALUE).multiply(BigDecimal.valueOf(100)),
      BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.valueOf(100)));

  private JavaMaths() {
  }

  /**
   * Constructs a new uniform big integer double generators bounded by {@link #DEFAULT_BIG_INTEGER_RANGE}.
   *
   * @return bounded uniform big integer generators
   */
  @Gen
  public static Generator<BigInteger> bigIntegerGen() {
    return bigIntegerGen(DEFAULT_BIG_INTEGER_RANGE);
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
   * Constructs a new uniform big decimal generators bounded by {@link #DEFAULT_BIG_DECIMAL_RANGE}.
   *
   * @return bounded uniform big decimal generators
   */
  @Gen
  public static Generator<BigDecimal> bigDecimalGen() {
    return bigDecimalGen(DEFAULT_BIG_DECIMAL_RANGE);
  }

  /**
   * Constructs a new uniform big decimal generators bounded by given range
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

  /**
   * Returns a math context generator selecting between IEEE 754R contexts:
   * <ul>
   *   <li>{@link MathContext#DECIMAL32}</li>
   *   <li>{@link MathContext#DECIMAL64}</li>
   *   <li>{@link MathContext#DECIMAL128}</li>
   *   <li>{@link MathContext#UNLIMITED}</li>
   * </ul>
   *
   * @return math context generator
   */
  @Gen
  public static Generator<MathContext> ieeeMathContextGen() {
    return Generators.oneOf(MathContext.DECIMAL32,
        MathContext.DECIMAL64,
        MathContext.DECIMAL128,
        MathContext.UNLIMITED);
  }

  /**
   * Returns a undefined math context generator with precision in [1, 256]
   * and all possible values of {@link RoundingMode}.
   *
   * @return math context generator
   */
  public static Generator<MathContext> mathContextGen() {
    Generator<Integer> precisionGen = integerGen(Ranges.closed(1, 256));
    Generator<RoundingMode> modeGen = Generators.oneOf(RoundingMode.values());
    return re -> new MathContext(precisionGen.get(re), modeGen.get(re));
  }

  /**
   * Returns a registry containing generator for classes in {@link java.math}.
   * <p>
   * The contained generators are :<br>
   * <ul>
   *   <li>{@link BigInteger}: {@link #bigIntegerGen()}</li>
   *   <li>{@link BigDecimal}: {@link #bigDecimalGen()}</li>
   *   <li>{@link MathContext}: {@link #ieeeMathContextGen()}</li>
   * </ul>
   *
   * @return registry of classes of {@link java.math}
   */
  public static Registry mathRegistry() {
    return Registries.forClass(JavaMaths.class);
  }
}
