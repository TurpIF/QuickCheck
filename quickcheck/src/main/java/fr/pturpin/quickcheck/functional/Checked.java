package fr.pturpin.quickcheck.functional;

import com.google.common.base.Preconditions;

import java.util.function.Function;

/**
 * Created by turpif on 21/05/17.
 */
public final class Checked {

  private Checked() {
    // nothing
  }

  public interface CheckedFunction<T, R, X extends Exception> {
    R apply(T t) throws X;

    default <V> CheckedFunction<T, V, X> andThen(Function<? super R, ? extends V> f) {
      Preconditions.checkNotNull(f);
      return t -> f.apply(this.apply(t));
    }

    static <T, R> Function<T, R> unchecked(CheckedFunction<T, R, ?> function) {
      return t -> {
        try {
          return function.apply(t);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      };
    }
  }

  public interface CheckedSupplier<T, X extends Exception> {
    T get() throws X;
  }

  public interface CheckedConsumer<T, X extends Exception> {
    void accept(T value) throws X;
  }

}
