package com.pturpin.quickcheck.test;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by turpif on 27/04/17.
 */
public final class TestResult {

  private final Throwable cause;
  private final long nbSkipped;
  private final long nbTotal;

  private TestResult(long nbSkipped, long nbTotal) {
    checkArgument(nbSkipped >= 0);
    checkArgument(nbTotal >= nbSkipped);
    this.cause = null;
    this.nbSkipped = nbSkipped;
    this.nbTotal = nbTotal;
  }

  private TestResult(long nbTotal) {
    checkArgument(nbTotal >= 0);
    this.cause = null;
    this.nbSkipped = 0;
    this.nbTotal = nbTotal;
  }

  private TestResult(Throwable cause) {
    this.cause = checkNotNull(cause);
    this.nbTotal = 1;
    this.nbSkipped = 0;
  }

  public static TestResult empty() {
    return new TestResult(0);
  }

  public static TestResult ok() {
    return new TestResult(1);
  }

  public static TestResult skipped() {
    return new TestResult(1, 1);
  }

  public static TestResult failure(Throwable cause) {
    return new TestResult(cause);
  }

  public Optional<Throwable> getFailureCause() {
    return Optional.ofNullable(cause);
  }

  public TestState getState() {
    return cause != null ? TestState.FAILURE : nbSkipped == 0 ? TestState.OK : TestState.SKIPPED;
  }

  public long getNbSkipped() {
    return nbSkipped;
  }

  public long getNbTotal() {
    return nbTotal;
  }

  public static TestResult merge(TestResult left, TestResult right) {
    boolean leftFail = left.getState() == TestState.FAILURE;
    boolean rightFail = right.getState() == TestState.FAILURE;
    if (leftFail || rightFail) {
      if (leftFail && rightFail) {
        Throwable leftErr = left.getFailureCause().get();
        Throwable rightErr = right.getFailureCause().get();
        leftErr.addSuppressed(rightErr);
        return new TestResult(leftErr);
      } else if (leftFail) {
        return left;
      }
      return right;
    }
    return new TestResult(left.nbSkipped + right.nbSkipped, left.nbTotal + right.nbTotal);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestResult that = (TestResult) o;
    if (cause != null ? !cause.equals(that.cause) : that.cause != null) {
      return false;
    }
    return nbSkipped == that.nbSkipped && nbTotal == that.nbTotal;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cause, nbSkipped, nbTotal);
  }

  public enum TestState {
    OK,
    FAILURE,
    SKIPPED
  }

  public static TestResult when(boolean guard, Runnable runnable) {
    if (!guard) {
      return TestResult.skipped();
    }
    runnable.run();
    return TestResult.ok();
  }
}
