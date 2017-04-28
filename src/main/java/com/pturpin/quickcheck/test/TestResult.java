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

  public enum TestState {
    OK,
    FAILURE,
    SKIPPED
  }

  public static TestResult guard(boolean guard, Runnable runnable) {
    if (!guard) {
      return TestResult.skipped();
    }
    runnable.run();
    return TestResult.ok();
  }
}
