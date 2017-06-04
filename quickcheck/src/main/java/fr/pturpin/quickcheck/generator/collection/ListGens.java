package fr.pturpin.quickcheck.generator.collection;

import com.google.common.collect.ImmutableList;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static fr.pturpin.quickcheck.generator.CastGens.intToLong;

/**
 * Created by pturpin on 04/06/2017.
 */
public final class ListGens {

  private ListGens() {
    // nothing
  }

  /**
   * Returns a new array list generator which size is controlled by given int generator
   * and which is filled by elements fetch from given generator.
   *
   * The generated list are instances of {@link ArrayList}.
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @see ArrayList
   *
   * @param elementGen generator of elements in lists
   * @param sizeGen generator of size of lists
   * @param <T> type of elements in lists
   * @return array list generator
   * @throws NullPointerException if element generator or size generator are null
   */
  public static <T> Generator<List<T>> arrayListGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return collectedListGen(elementGen, sizeGen, Collectors.toCollection(ArrayList::new));
  }

  /**
   * Returns a new linked list generator which size is controlled by given int generator
   * and which is filled by elements fetch from given generator.
   *
   * The generated list are instances of {@link LinkedList}.
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @see LinkedList
   *
   * @param elementGen generator of elements in lists
   * @param sizeGen generator of size of lists
   * @param <T> type of elements in lists
   * @return linked list generator
   * @throws NullPointerException if element generator or size generator are null
   */
  public static <T> Generator<List<T>> linkedListGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return collectedListGen(elementGen, sizeGen, Collectors.toCollection(LinkedList::new));
  }

  /**
   * Returns a new immutable list generator which size is controlled by given int generator
   * and which is filled by elements fetch from given generator.
   *
   * The generated list are instances of {@link ImmutableList}.
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   * As {@link ImmutableList} does not accept any null values, if the element generator yield a null, a {@link NullPointerException} is thrown.
   *
   * @see ImmutableList
   *
   * @param elementGen generator of elements in lists
   * @param sizeGen generator of size of lists
   * @param <T> type of elements in lists
   * @return immutable list generator
   * @throws NullPointerException if element generator or size generator are null
   */
  public static <T> Generator<ImmutableList<T>> immutableListGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return collectedListGen(elementGen, sizeGen, toImmutableList());
  }

  /**
   * Returns a new list from collected finite stream generator.
   *
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @param elementGen generator of elements in list
   * @param sizeGen size generator
   * @param collector collector to build list
   * @param <T> type of element in list
   * @param <L> type of the list
   * @return collected list generator
   * @throws NullPointerException if any parameters is null
   */
  public static <T, L extends List<T>> Generator<L> collectedListGen(Generator<T> elementGen, Generator<Integer> sizeGen, Collector<T, ?, L> collector) {
    checkNotNull(collector);
    return Generators.map(StreamGens.finiteGen(elementGen, intToLong(sizeGen)), streamGen -> streamGen.collect(collector));
  }
}
