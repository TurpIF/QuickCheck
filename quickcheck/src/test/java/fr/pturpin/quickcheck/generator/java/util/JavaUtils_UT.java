package fr.pturpin.quickcheck.generator.java.util;

import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.test.configuration.DefaultRegistryFactory;
import org.junit.Test;

import java.util.*;

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
    assertIsInDefaultRegistry(Collection.class);
  }

  @Test
  public void comparatorShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Comparator.class);
  }

  @Test
  public void dequeShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Deque.class);
  }

  @Test
  public void iteratorShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Iterator.class);
  }

  @Test
  public void listShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(List.class);
  }

  @Test
  public void listIteratorShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(ListIterator.class);
  }

  @Test
  public void mapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Map.class);
  }

  @Test
  public void mapEntryShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Map.Entry.class);
  }

  @Test
  public void navigableMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(NavigableMap.class);
  }

  @Test
  public void navigableSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(NavigableSet.class);
  }

  @Test
  public void queueShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Queue.class);
  }

  @Test
  public void setShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Set.class);
  }

  @Test
  public void sortedMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(SortedMap.class);
  }

  @Test
  public void sortedSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(SortedSet.class);
  }

  @Test
  public void timeZoneShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(TimeZone.class);
  }

  /// Classes

  @Test
  public void arrayDequeShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(ArrayDeque.class);
  }

  @Test
  public void arrayListShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(ArrayList.class);
  }

  @Test
  public void bitSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(BitSet.class);
  }

  @Test
  public void currencyShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Currency.class);
  }

  @Test
  public void enumMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(EnumMap.class, ENUM_FILLER);
  }

  @Test
  public void enumSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(EnumSet.class, ENUM_FILLER);
  }

  @Test
  public void hashMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(HashMap.class);
  }

  @Test
  public void hashSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(HashSet.class);
  }

  @Test
  public void identityHashMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(IdentityHashMap.class);
  }

  @Test
  public void linkedHashMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(LinkedHashMap.class);
  }

  @Test
  public void linkedHashSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(LinkedHashSet.class);
  }

  @Test
  public void linkedListShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(LinkedList.class);
  }

  @Test
  public void localeShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Locale.class);
  }

  @Test
  public void priorityQueueShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(PriorityQueue.class);
  }

  @Test
  public void randomShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(Random.class);
  }

  @Test
  public void treeMapShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(TreeMap.class);
  }

  @Test
  public void treeSetShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(TreeSet.class);
  }

  @Test
  public void uuidShouldBeInDefaultRegistry() {
    assertIsInDefaultRegistry(UUID.class);
  }

  @Gen
  public static Generator<MyEnum> myEnumGen() {
    return Generators.oneOf(MyEnum.values());
  }

  private enum MyEnum { FIRST, SECOND, THIRD }

  private static Registry getRegistry() {
    Registry defaultRegistry = new DefaultRegistryFactory().create();
    Registry klassRegistry = Registries.forClass(JavaUtils_UT.class);
    return Registries.alternatives(defaultRegistry, klassRegistry);
  }

  private static <T> void assertIsInDefaultRegistry(Class<T> klass) {
    assertIsInRegistry(getRegistry(), klass, k -> getIdentifier(k, PARAM_FILLER));
  }

  private static <T> void assertIsInDefaultRegistry(Class<T> klass, TypeIdentifier<?> filler) {
    assertIsInRegistry(getRegistry(), klass, k -> getIdentifier(k, filler));
  }
}
