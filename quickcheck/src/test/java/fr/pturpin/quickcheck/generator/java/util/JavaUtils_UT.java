package fr.pturpin.quickcheck.generator.java.util;

import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.test.configuration.DefaultRegistryFactory;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static fr.pturpin.quickcheck.generator.RegistryAssertions.assertIsInRegistry;
import static fr.pturpin.quickcheck.generator.RegistryAssertions.getIdentifier;
import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

/**
 * Created by turpif on 12/06/17.
 */
public class JavaUtils_UT {

  private static final TypeIdentifier<?> PARAM_FILLER = classId(double.class);
  private static final TypeIdentifier<?> ENUM_FILLER = classId(MyEnum.class);

  /// Interfaces

  @Test
  public void collectionShouldBeInDefaultRegistry() {
    assertIsInRegistries(Collection.class);
  }

  @Test
  public void comparatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(Comparator.class);
  }

  @Test
  public void dequeShouldBeInDefaultRegistry() {
    assertIsInRegistries(Deque.class);
  }

  @Test
  public void iteratorShouldBeInDefaultRegistry() {
    assertIsInRegistries(Iterator.class);
  }

  @Test
  public void listShouldBeInDefaultRegistry() {
    assertIsInRegistries(List.class);
  }

  @Test
  public void listIteratorShouldBeInDefaultRegistry() {
    assertIsInRegistries(ListIterator.class);
  }

  @Test
  public void mapShouldBeInDefaultRegistry() {
    assertIsInRegistries(Map.class);
  }

  @Test
  public void mapEntryShouldBeInDefaultRegistry() {
    assertIsInRegistries(Map.Entry.class);
  }

  @Test
  public void navigableMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(NavigableMap.class);
  }

  @Test
  public void navigableSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(NavigableSet.class);
  }

  @Test
  public void queueShouldBeInDefaultRegistry() {
    assertIsInRegistries(Queue.class);
  }

  @Test
  public void setShouldBeInDefaultRegistry() {
    assertIsInRegistries(Set.class);
  }

  @Test
  public void sortedMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(SortedMap.class);
  }

  @Test
  public void sortedSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(SortedSet.class);
  }

  @Test
  public void timeZoneShouldBeInDefaultRegistry() {
    assertIsInRegistries(TimeZone.class);
  }

  /// Classes

  @Test
  public void arrayDequeShouldBeInDefaultRegistry() {
    assertIsInRegistries(ArrayDeque.class);
  }

  @Test
  public void arrayListShouldBeInDefaultRegistry() {
    assertIsInRegistries(ArrayList.class);
  }

  @Test
  public void bitSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(BitSet.class);
  }

  @Test
  public void currencyShouldBeInDefaultRegistry() {
    assertIsInRegistries(Currency.class);
  }

  @Test
  public void enumMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(EnumMap.class, ENUM_FILLER);
  }

  @Test
  public void enumSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(EnumSet.class, ENUM_FILLER);
  }

  @Test
  public void hashMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(HashMap.class);
  }

  @Test
  public void hashSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(HashSet.class);
  }

  @Test
  public void identityHashMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(IdentityHashMap.class);
  }

  @Test
  public void linkedHashMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(LinkedHashMap.class);
  }

  @Test
  public void linkedHashSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(LinkedHashSet.class);
  }

  @Test
  public void linkedListShouldBeInDefaultRegistry() {
    assertIsInRegistries(LinkedList.class);
  }

  @Test
  public void localeShouldBeInDefaultRegistry() {
    assertIsInRegistries(Locale.class);
  }

  @Test
  public void priorityQueueShouldBeInDefaultRegistry() {
    assertIsInRegistries(PriorityQueue.class);
  }

  @Test
  public void randomShouldBeInDefaultRegistry() {
    assertIsInRegistries(Random.class);
  }

  @Test
  public void treeMapShouldBeInDefaultRegistry() {
    assertIsInRegistries(TreeMap.class);
  }

  @Test
  public void treeSetShouldBeInDefaultRegistry() {
    assertIsInRegistries(TreeSet.class);
  }

  @Test
  public void uuidShouldBeInDefaultRegistry() {
    assertIsInRegistries(UUID.class);
  }

  private enum MyEnum { FIRST, SECOND, THIRD }

  private static Stream<Registry> getRegistries() {
    return Stream.of(
        Registries.alternatives(
            new DefaultRegistryFactory().create(),
            Registries.builder()
                .put(classId(MyEnum.class), Generators.oneOf(MyEnum.values()))
                .build()),
        Registries.alternatives(
            JavaUtils.utilsRegistry(),
            Registries.builder()
                .put(classId(MyEnum.class), Generators.oneOf(MyEnum.values()))
                .put(classId(long.class), NumberGens.longGen())
                .put(classId(int.class), NumberGens.integerGen(Ranges.closed(0, 20)))
                .put(classId(double.class), NumberGens.doubleGen())
                .build()));
  }

  private static <T> void assertIsInRegistries(Class<T> klass) {
    getRegistries().forEach(registry -> assertIsInRegistry(registry, klass, k -> getIdentifier(k, PARAM_FILLER)));
  }

  private static <T> void assertIsInRegistries(Class<T> klass, TypeIdentifier<?> filler) {
    getRegistries().forEach(registry -> assertIsInRegistry(registry, klass, k -> getIdentifier(k, filler)));
  }
}
