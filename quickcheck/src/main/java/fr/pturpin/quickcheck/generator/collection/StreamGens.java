package fr.pturpin.quickcheck.generator.collection;

import fr.pturpin.quickcheck.generator.Generator;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by pturpin on 04/06/2017.
 */
public final class StreamGens {

  private StreamGens() {
    // nothing
  }

  /**
   * Returns a stream finite generator of elements of given generator
   *
   * If the limit generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @param elementGen element of stream generator
   * @param limitGen size of stream generator
   * @param <T> type of element in stream
   * @return finite stream generator
   * @throws NullPointerException if any parameters is null
   */
  public static <T> Generator<Stream<T>> finiteGen(Generator<T> elementGen, Generator<Long> limitGen) {
    checkNotNull(elementGen);
    checkNotNull(limitGen);
    return re -> {
      long limit = limitGen.get(re);
      checkState(limit >= 0);
      return Stream.generate(() -> elementGen.get(re)).limit(limit);
    };
  }

  /**
   * Returns a stream infinite generator of elements of given generator
   *
   * @param elementGen element of stream generator
   * @param <T> type of element in stream
   * @return infinite stream generator
   * @throws NullPointerException if generator is null
   */
  public static <T> Generator<Stream<T>> infiniteGen(Generator<T> elementGen) {
    checkNotNull(elementGen);
    return re -> Stream.generate(() -> elementGen.get(re));
  }
}
