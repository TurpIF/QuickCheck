package fr.pturpin.quickcheck.test.configuration;

import com.google.common.collect.Streams;
import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.base.Ranges.Range;
import fr.pturpin.quickcheck.functional.Function3;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.generator.java.util.JavaUtils;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registries.RegistryBuilder;
import fr.pturpin.quickcheck.registry.Registry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;
import static fr.pturpin.quickcheck.identifier.Identifiers.paramId;
import static fr.pturpin.quickcheck.registry.Registries.DynamicRegistry.resolved;

/**
 * Created by turpif on 28/04/17.
 */
public final class DefaultRegistryFactory implements RegistryFactory {

  public DefaultRegistryFactory() {
    // nothing
  }

  @Override
  public Registry create() {
    Range<BigInteger> bigIntegerRange = Ranges.closed(
        BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.valueOf(1000)),
        BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.valueOf(1000)));

    Range<BigDecimal> bigDecimalRange = Ranges.closed(
        BigDecimal.valueOf(-Double.MAX_VALUE).multiply(BigDecimal.valueOf(1000)),
        BigDecimal.valueOf(Double.MIN_VALUE).multiply(BigDecimal.valueOf(1000)));

    RegistryBuilder builder = Registries.builder()
        .put(classId(double.class), NumberGens.doubleGen())
        .put(classId(int.class), NumberGens.integerGen())
        .put(classId(long.class), NumberGens.longGen())
        .put(classId(BigInteger.class), NumberGens.bigIntegerGen(bigIntegerRange))
        .put(classId(BigDecimal.class), NumberGens.bigDecimalGen(bigDecimalRange))
        .putDyn(Supplier.class, resolved(gen -> Generators.map(gen, v -> () -> v)));

    return putJavaUtils(builder).build();
  }

  private static RegistryBuilder putJavaUtils(RegistryBuilder builder) {
    Generator<Integer> sizeGen = NumberGens.integerGen(Ranges.closed(0, 100));

    Function3<Registry, TypeIdentifier<Object>, TypeIdentifier<Object>, Optional<Generator<AbstractMap.SimpleEntry<Object, Object>>>> simpleGenGen = resolved(JavaUtils::simpleEntryGen);

    return builder
        .putDyn(Comparator.class, (registry, type) -> {
          if (Comparable.class.isAssignableFrom(type.getTypeClass())) {
            Generator<Comparator> comparatorGenerator = (Generator) JavaUtils.comparatorGen();
            return Optional.of(comparatorGenerator);
          }
          return Optional.empty();
        })

        .putDyn(Iterator.class, fromHierachy1(Iterable.class).andThen(optGen -> optGen.map(gen -> Generators.map(gen, Iterable::iterator))))
        .putDyn(ListIterator.class, fromHierachy1(List.class).andThen(optGen -> optGen.map(gen -> Generators.map(gen, List::listIterator))))

        .putDyn(Iterable.class, fromHierachy1(Collection.class))
        .putDyn(Collection.class, fromHierachy1(Set.class, List.class, Queue.class))

        .putDyn(Map.Entry.class, fromHierachy2(AbstractMap.SimpleEntry.class))
        .putDyn(AbstractMap.SimpleEntry.class, simpleGenGen)

        .putDyn(Map.class, fromHierachy2(HashMap.class, EnumMap.class, LinkedHashMap.class, SortedMap.class))
        .putDyn(EnumMap.class, (registry, keyId, valueId) -> {
          Class<?> keyClass = keyId.getTypeClass();
          if (!Enum.class.isAssignableFrom(keyClass)) {
            return Optional.empty();
          }
          Class<Enum> enumKeyClass = (Class<Enum>) keyClass;
          return (Optional) mapGen(entryGen -> JavaUtils.enumMapGen(enumKeyClass, (Generator) entryGen, sizeGen))
              .apply(registry, keyId, valueId);
        })
        .putDyn(HashMap.class, mapGen(entryGen -> JavaUtils.hashMapGen(entryGen, sizeGen)))
        .putDyn(IdentityHashMap.class, mapGen(entryGen -> JavaUtils.identityHashMapGen(entryGen, sizeGen)))
        .putDyn(LinkedHashMap.class, mapGen(entryGen -> JavaUtils.linkedHashMapGen(entryGen, sizeGen)))

        .putDyn(SortedMap.class, fromHierachy2(NavigableMap.class))
        .putDyn(NavigableMap.class, fromHierachy2(TreeMap.class))
        .putDyn(TreeMap.class, sortedMapGen((entryGen, comparatorGen) -> JavaUtils.treeMapGen(entryGen, sizeGen, comparatorGen)))

        .putDyn(Set.class, fromHierachy1(HashSet.class, EnumSet.class, LinkedHashSet.class, SortedSet.class))
        .putDyn(EnumSet.class, (registry, elementId) -> {
          Class<?> elemClass = elementId.getTypeClass();
          if (!Enum.class.isAssignableFrom(elemClass)) {
            return Optional.empty();
          }
          Class<Enum> enumElemClass = (Class<Enum>) elemClass;
          return registry.lookup(elementId)
              .map(elementGen -> (Generator<Enum>) (Generator) elementGen)
              .map(elementGen -> JavaUtils.enumSetGen(enumElemClass, elementGen, sizeGen));
        })
        .putDyn(HashSet.class, resolved(gen -> JavaUtils.hashSetGen(gen, sizeGen)))
        .putDyn(LinkedHashSet.class, resolved(gen -> JavaUtils.linkedHashSetGen(gen, sizeGen)))

        .putDyn(SortedSet.class, fromHierachy1(NavigableSet.class))
        .putDyn(NavigableSet.class, fromHierachy1(TreeSet.class))
        .putDyn(TreeSet.class, comparedGen((elementGen, comparatorGen) -> JavaUtils.treeSetGen(elementGen, sizeGen, comparatorGen)))

        .putDyn(List.class, fromHierachy1(ArrayList.class, LinkedList.class))
        .putDyn(ArrayList.class, resolved(gen -> JavaUtils.arrayListGen(gen, sizeGen)))
        .putDyn(LinkedList.class, resolved(gen -> JavaUtils.linkedListGen(gen, sizeGen)))

        .putDyn(Deque.class, fromHierachy1(ArrayDeque.class))
        .putDyn(ArrayDeque.class, resolved(gen -> JavaUtils.arrayDequeGen(gen, sizeGen)))

        .putDyn(Queue.class, fromHierachy1(PriorityQueue.class, Deque.class))
        .putDyn(PriorityQueue.class, comparedGen((elementGen, comparatorGen) -> JavaUtils.priorityQueueGen(elementGen, sizeGen, comparatorGen)))

        .put(classId(BitSet.class), Registries.StaticRegistry.resolved(classId(long.class), gen -> JavaUtils.bitSetGen(gen, sizeGen)))
        .put(classId(Currency.class), JavaUtils.availableCurrencyGen())
        .put(classId(Locale.class), JavaUtils.isoLocaleGen())
        .put(classId(Random.class), Registries.StaticRegistry.resolved(classId(long.class), JavaUtils::randomGen))
        .put(classId(TimeZone.class), Generators.oneGenOf(JavaUtils.availableTimeZoneGen(), JavaUtils.allTimeZoneGen()))
        .put(classId(UUID.class), Registries.StaticRegistry.resolved(classId(long.class), JavaUtils::randomUUIDGen));
  }

  @SafeVarargs
  private static <T, A> BiFunction<Registry, TypeIdentifier<A>, Optional<Generator<T>>> fromHierachy1(Class<? extends T>... klasses) {
    return (registry, firstType) -> {
      List<Generator<? extends T>> generators = Arrays.stream(klasses)
          .map(klass -> registry.lookup(paramId((Class<T>) klass, firstType)))
          .flatMap(Streams::stream)
          .collect(Collectors.toList());
      return generators.isEmpty() ? Optional.empty() : Optional.of(Generators.oneGenOf(generators));
    };
  }

  @SafeVarargs
  private static <T, A, B> Function3<Registry, TypeIdentifier<A>, TypeIdentifier<B>, Optional<Generator<T>>> fromHierachy2(Class<? extends T>... klasses) {
    return (registry, firstType, secondType) -> {
      List<Generator<? extends T>> generators = Arrays.stream(klasses)
          .map(klass -> registry.lookup(paramId((Class<T>) klass, firstType, secondType)))
          .flatMap(Streams::stream)
          .collect(Collectors.toList());
      return generators.isEmpty() ? Optional.empty() : Optional.of(Generators.oneGenOf(generators));
    };
  }

  private static <K, V, M extends Map<K, V>> Function3<Registry, TypeIdentifier<K>, TypeIdentifier<V>, Optional<Generator<M>>> mapGen(Function<Generator<Map.Entry<K, V>>, Generator<M>> mapper) {
    return (registry, keyId, valueId) -> registry.lookup(paramId(Map.Entry.class, keyId, valueId))
        .map(entryGen -> {
          Generator<Map.Entry<K, V>> castedEntryGen = (Generator) entryGen;
          return mapper.apply(castedEntryGen);
        });
  }

  private static <K, V, M extends Map<K, V>> Function3<Registry, TypeIdentifier<K>, TypeIdentifier<V>, Optional<Generator<M>>> sortedMapGen(BiFunction<Generator<Map.Entry<K, V>>, Generator<Comparator<? super K>>, Generator<M>> mapper) {
    return (registry, keyId, valueId) -> registry.lookup(paramId(Map.Entry.class, keyId, valueId))
        .flatMap(entryGen -> registry.lookup(paramId(Comparator.class, keyId))
            .map(comparatorGen -> {
              Generator<Map.Entry<K, V>> entryG = (Generator) entryGen;
              Generator<Comparator<? super K>> comparatorG = (Generator) comparatorGen;
              return mapper.apply(entryG, comparatorG);
            }));
  }

  private static <T, E, C> BiFunction<Registry, TypeIdentifier<E>, Optional<Generator<T>>> comparedGen(BiFunction<Generator<E>, Generator<Comparator<? super C>>, Generator<T>> mapper) {
    return (registry, typeId) -> registry.lookup(typeId)
        .flatMap(elementGen -> registry.lookup(paramId(Comparator.class, typeId))
            .map(comparatorGen -> {
              Generator<Comparator<? super C>> comparatorG = (Generator) comparatorGen;
              return mapper.apply(elementGen, comparatorG);
            }));
  }
}
