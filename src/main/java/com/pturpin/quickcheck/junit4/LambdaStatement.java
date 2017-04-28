package com.pturpin.quickcheck.junit4;

import org.junit.runners.model.Statement;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by turpif on 28/04/17.
 */
final class LambdaStatement extends Statement {
  private final ThrowingRunnable runnable;

  private LambdaStatement(ThrowingRunnable runnable) {
    this.runnable = checkNotNull(runnable);
  }

  static Statement of(ThrowingRunnable runnable) {
    return new LambdaStatement(runnable);
  }

  @Override
  public void evaluate() throws Throwable {
    runnable.run();
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Throwable;
  }
}
