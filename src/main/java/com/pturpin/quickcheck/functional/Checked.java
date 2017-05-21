package com.pturpin.quickcheck.functional;

/**
 * Created by turpif on 21/05/17.
 */
public final class Checked {

  private Checked() {
    // nothing
  }

  public interface CheckedSupplier<T, X extends Exception> {
    T get() throws X;
  }

  public interface CheckedConsumer<T, X extends Exception> {
    void accept(T value) throws X;
  }

}
