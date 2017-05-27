package com.pturpin.quickcheck.base;

import com.google.common.base.Preconditions;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * Created by turpif on 13/05/17.
 */
public final class Ranges {
  private Ranges() {
    // nothing
  }

  public static <T extends Comparable<T>> Range<T> opened(T min, T max) {
    return new Range<>(min, max, false, false);
  }

  public static DoubleRange opened(double min, double max) {
    return new DoubleRange(min, max, false, false);
  }

  public static IntRange opened(int min, int max) {
    return new IntRange(min, max, false, false);
  }

  public static LongRange opened(long min, long max) {
    return new LongRange(min, max, false, false);
  }

  public static <T extends Comparable<T>> Range<T> closed(T min, T max) {
    return new Range<>(min, max, true, true);
  }

  public static DoubleRange closed(double min, double max) {
    return new DoubleRange(min, max, true, true);
  }

  public static IntRange closed(int min, int max) {
    return new IntRange(min, max, true, true);
  }

  public static LongRange closed(long min, long max) {
    return new LongRange(min, max, true, true);
  }

  public static Range<Integer> boxed(IntRange range) {
    return new Range<>(range.getLeft(), range.getRight(), range.isLeftClosed(), range.isRightClosed());
  }

  public static Range<Long> boxed(LongRange range) {
    return new Range<>(range.getLeft(), range.getRight(), range.isLeftClosed(), range.isRightClosed());
  }

  public static Range<Double> boxed(DoubleRange range) {
    return new Range<>(range.getLeft(), range.getRight(), range.isLeftClosed(), range.isRightClosed());
  }

  /**
   * Transforms the bounds of the given range with the given mapper function.
   * Resulting bounds should stay valid, that is the left one should be lesser or equal than the right one.
   * This can be guaranteed if the given mapper is a monotonically increasing function.
   *
   * @param range
   * @param mapper
   * @return
   */
  public static <T extends Comparable<T>, R extends Comparable<R>> Range<R> map(Range<T> range, Function<T, R> mapper) {
    R left = mapper.apply(range.getLeft());
    R right = mapper.apply(range.getRight());
    boolean isEmpty = left.compareTo(right) == 0 && (!range.isLeftClosed() || !range.isRightClosed());
    return new Range<>(left, right, !isEmpty && range.isLeftClosed(), !isEmpty && range.isRightClosed());
  }

  public static IntRange map(IntRange range, IntUnaryOperator mapper) {
    int left = mapper.applyAsInt(range.getLeft());
    int right = mapper.applyAsInt(range.getRight());
    boolean isEmpty = left == right && (!range.isLeftClosed() || !range.isRightClosed());
    return new IntRange(left, right, !isEmpty && range.isLeftClosed(), !isEmpty && range.isRightClosed());
  }

  public static LongRange map(LongRange range, LongUnaryOperator mapper) {
    long left = mapper.applyAsLong(range.getLeft());
    long right = mapper.applyAsLong(range.getRight());
    boolean isEmpty = left == right && (!range.isLeftClosed() || !range.isRightClosed());
    return new LongRange(left, right, !isEmpty && range.isLeftClosed(), !isEmpty && range.isRightClosed());
  }

  public static DoubleRange map(DoubleRange range, DoubleUnaryOperator mapper) {
    double left = mapper.applyAsDouble(range.getLeft());
    double right = mapper.applyAsDouble(range.getRight());
    boolean isEmpty = left == right && (!range.isLeftClosed() || !range.isRightClosed());
    return new DoubleRange(left, right, !isEmpty && range.isLeftClosed(), !isEmpty && range.isRightClosed());
  }

  public static final class Range<T extends Comparable<T>> {

    private final T min;
    private final T max;
    private final boolean minIncluded;
    private final boolean maxIncluded;

    private Range(T min, T max, boolean minIncluded, boolean maxIncluded) {
      this.min = Preconditions.checkNotNull(min);
      this.max = Preconditions.checkNotNull(max);
      this.minIncluded = minIncluded;
      this.maxIncluded = maxIncluded;

      Preconditions.checkArgument(min.compareTo(max) <= 0);
      Preconditions.checkArgument(min.compareTo(max) != 0 || minIncluded == maxIncluded);
    }

    public boolean contains(T value) {
      return (minIncluded ? value.compareTo(min) >= 0 : value.compareTo(min) > 0)
             && (maxIncluded ? value.compareTo(max) <= 0 : value.compareTo(max) < 0);
    }

    public boolean isEmpty() {
      return min.compareTo(max) == 0 && !minIncluded && !maxIncluded;
    }

    public T getLeft() {
      return min;
    }

    public T getRight() {
      return max;
    }

    public boolean isLeftClosed() {
      return minIncluded;
    }

    public boolean isRightClosed() {
      return maxIncluded;
    }

    @Override
    public String toString() {
      return (minIncluded ? "[" : "(") + min + " ; " + max + (maxIncluded ? "]" : ")");
    }
  }

  public static final class DoubleRange {

    private final double min;
    private final double max;
    private final boolean minIncluded;
    private final boolean maxIncluded;

    private DoubleRange(double min, double max, boolean minIncluded, boolean maxIncluded) {
      Preconditions.checkArgument(!Double.isNaN(min));
      Preconditions.checkArgument(!Double.isNaN(max));
      Preconditions.checkArgument(min <= max);
      Preconditions.checkArgument(min != max || minIncluded == maxIncluded);
      this.min = min + 0.d; // Remove distinction between -0 and 0
      this.max = max + 0.d;
      this.minIncluded = minIncluded;
      this.maxIncluded = maxIncluded;
    }

    public boolean contains(double value) {
      return (minIncluded ? value >= min : value > min) && (maxIncluded ? value <= max : value < max);
    }

    public boolean isEmpty() {
      return (min == max || min == max - Double.MIN_VALUE) && !minIncluded && !maxIncluded;
    }

    public double getLeft() {
      return min;
    }

    public double getRight() {
      return max;
    }

    public boolean isLeftClosed() {
      return minIncluded;
    }

    public boolean isRightClosed() {
      return maxIncluded;
    }

    @Override
    public String toString() {
      return (minIncluded ? "[" : "(") + min + " ; " + max + (maxIncluded ? "]" : ")");
    }
  }

  public static final class IntRange {

    private final int min;
    private final int max;
    private final boolean minIncluded;
    private final boolean maxIncluded;

    private IntRange(int min, int max, boolean minIncluded, boolean maxIncluded) {
      Preconditions.checkArgument(min <= max);
      Preconditions.checkArgument(min != max || minIncluded == maxIncluded);
      this.min = min;
      this.max = max;
      this.minIncluded = minIncluded;
      this.maxIncluded = maxIncluded;
    }

    public boolean contains(int value) {
      return (minIncluded ? value >= min : value > min) && (maxIncluded ? value <= max : value < max);
    }

    public boolean isEmpty() {
      return (min == max || min == max - 1) && !minIncluded && !maxIncluded;
    }

    public int getLeft() {
      return min;
    }

    public int getRight() {
      return max;
    }

    public boolean isLeftClosed() {
      return minIncluded;
    }

    public boolean isRightClosed() {
      return maxIncluded;
    }

    @Override
    public String toString() {
      return (minIncluded ? "[" : "(") + min + " ; " + max + (maxIncluded ? "]" : ")");
    }
  }

  public static final class LongRange {

    private final long min;
    private final long max;
    private final boolean minIncluded;
    private final boolean maxIncluded;

    private LongRange(long min, long max, boolean minIncluded, boolean maxIncluded) {
      Preconditions.checkArgument(min <= max);
      Preconditions.checkArgument(min != max || minIncluded == maxIncluded);
      this.min = min;
      this.max = max;
      this.minIncluded = minIncluded;
      this.maxIncluded = maxIncluded;
    }

    public boolean contains(long value) {
      return (minIncluded ? value >= min : value > min) && (maxIncluded ? value <= max : value < max);
    }

    public boolean isEmpty() {
      return (min == max || min == max - 1) && !minIncluded && !maxIncluded;
    }

    public long getLeft() {
      return min;
    }

    public long getRight() {
      return max;
    }

    public boolean isLeftClosed() {
      return minIncluded;
    }

    public boolean isRightClosed() {
      return maxIncluded;
    }

    @Override
    public String toString() {
      return (minIncluded ? "[" : "(") + min + " ; " + max + (maxIncluded ? "]" : ")");
    }
  }
}
