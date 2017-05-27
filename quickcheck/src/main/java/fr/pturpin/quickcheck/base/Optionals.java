package fr.pturpin.quickcheck.base;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by turpif on 27/04/17.
 */
public final class Optionals {

  private Optionals() { /* Helper class */ }

  public static <T> Optional<T> guard(boolean condition, Supplier<Optional<T>> supplier) {
    if (!condition) {
      return Optional.empty();
    }
    return supplier.get();
  }

  public static <T, A, R> Collector<Optional<T>, Optional<A>, Optional<R>> allPresent(Collector<T, A, ? extends R> downstream) {
    return new AllPresentCollector<>(downstream);
  }

  private static final class AllPresentCollector<T, A, R> implements Collector<Optional<T>, Optional<A>, Optional<R>> {

    private volatile boolean hasEmpty;
    private final Collector<T, A, ? extends R> downstream;

    private AllPresentCollector(Collector<T, A, ? extends R> downstream) {
      this.downstream = checkNotNull(downstream);
      this.hasEmpty = false;
    }

    @Override
    public Supplier<Optional<A>> supplier() {
      return () -> Optional.of(downstream.supplier().get());
    }

    @Override
    public BiConsumer<Optional<A>, Optional<T>> accumulator() {
      return (optAcc, optVal) -> {
        if (!hasEmpty) {
          if (!optVal.isPresent()) {
            hasEmpty = true;
          } else {
            optAcc.ifPresent(acc -> downstream.accumulator().accept(acc, optVal.get()));
          }
        }
      };
    }

    @Override
    public BinaryOperator<Optional<A>> combiner() {
      return (optLeft, optRight) ->
          hasEmpty ? Optional.empty()
              : optLeft.flatMap(left ->
              optRight.map(right ->
                  downstream.combiner().apply(left, right)));
    }

    @Override
    public Function<Optional<A>, Optional<R>> finisher() {
      return opt -> opt.map(downstream.finisher());
    }

    @Override
    public Set<Characteristics> characteristics() {
      return downstream.characteristics();
    }
  }
}
