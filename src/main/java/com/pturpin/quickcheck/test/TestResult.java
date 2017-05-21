package com.pturpin.quickcheck.test;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by turpif on 27/04/17.
 */
public final class TestResult {

  private static final TestResult OK_INSTANCE = new TestResult(TestState.OK);
  private static final TestResult SKIPPED_INSTANCE = new TestResult(TestState.SKIPPED);

  private final Throwable cause;
  private final TestState state;

  private TestResult(TestState state) {
    checkArgument(state != TestState.FAILURE);
    this.cause = null;
    this.state = checkNotNull(state);
  }

  private TestResult(Throwable cause) {
    this.cause = checkNotNull(cause);
    this.state = TestState.FAILURE;
  }

  public static TestResult ok() {
    return OK_INSTANCE;
  }

  public static TestResult skipped() {
    return SKIPPED_INSTANCE;
  }

  public static TestResult failure(Throwable cause) {
    return new TestResult(cause);
  }

  public Optional<Throwable> getFailureCause() {
    return Optional.ofNullable(cause);
  }

  public TestState getState() {
    return state;
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
    return state == that.state;

  }

  @Override
  public int hashCode() {
    int result = cause != null ? cause.hashCode() : 0;
    result = 31 * result + state.hashCode();
    return result;
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
