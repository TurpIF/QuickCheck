package fr.pturpin.quickcheck.generator.java.util;

import com.google.common.collect.Streams;
import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.functional.Function3;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.collection.StreamGens;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.google.common.base.Preconditions.*;
import static fr.pturpin.quickcheck.generator.CastGens.intToLong;
import static fr.pturpin.quickcheck.identifier.Identifiers.classId;
import static fr.pturpin.quickcheck.identifier.Identifiers.paramId;

/**
 * Created by pturpin on 12/06/2017.
 */
public class JavaUtils {

  private static final int DEFAULT_MAX_TRY = 100;

  // Classes

  /**
   * Returns a new array deque generator which size is controlled by given int generator
   * and which is filled by elements fetch from given generator.
   *
   * The generated list are instances of {@link ArrayDeque}.
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @see ArrayDeque
   *
   * @param elementGen generator of elements in deques
   * @param sizeGen generator of size of deques
   * @param <T> type of elements in deques
   * @return array deque generator
   * @throws NullPointerException if element generator or size generator are null
   */
  @Gen
  public static <T> Generator<ArrayDeque<T>> arrayDequeGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return collectedGen(elementGen, sizeGen, Collectors.toCollection(ArrayDeque::new));
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
  @Gen
  public static <T> Generator<ArrayList<T>> arrayListGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return collectedGen(elementGen, sizeGen, Collectors.toCollection(ArrayList::new));
  }

  /**
   * Returns a new bitset generator which size is controlled by given int generator
   * and which is filled by bits of longs fetch from given generator.
   *
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @see BitSet
   *
   * @param bitsGen generator of elements in lists
   * @param sizeGen generator of size of lists
   * @return bitset generator
   * @throws NullPointerException if element generator or size generator are null
   */
  @Gen
  public static Generator<BitSet> bitSetGen(Generator<Long> bitsGen, Generator<Integer> sizeGen) {
    checkNotNull(bitsGen);
    checkNotNull(sizeGen);
    return re -> {
      int bitSize = sizeGen.get(re);
      checkState(bitSize >= 0);
      int longSize = (bitSize + Long.SIZE) / Long.SIZE;
      int cardinality = longSize * Long.SIZE;
      BitSet bitSet = BitSet.valueOf(LongStream.generate(() -> bitsGen.get(re)).limit(longSize).toArray());
      if (cardinality > bitSize) {
        bitSet.clear(bitSize + 1, cardinality);
      }
      return bitSet;
    };
  }

  /**
   * Returns a new currency generator picking currency in available currencies.
   *
   * @see Currency#getAvailableCurrencies()
   *
   * @return currency generator
   */
  @Gen
  public static Generator<Currency> availableCurrencyGen() {
    return Generators.oneOf(Currency.getAvailableCurrencies());
  }

  /**
   * Returns a new comparator generator which is one of natural or reverse order.
   *
   * @param <T> Type of compared object
   * @return comparator generator
   */
  @Gen
  public static <T extends Comparable<T>> Generator<Comparator<T>> comparatorGen() {
    return Generators.oneOf(Comparator.naturalOrder(), Comparator.reverseOrder());
  }

  /**
   * Returns a new enum map generator from given entry generator and size generator.
   * The map implementation is {@link EnumMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see EnumMap
   * @see #enumMapGen(Class, Generator, Generator)
   *
   * @param klass Class of keys
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param maxTry Maximum try to yield a new key
   * @param <K> type of keys
   * @param <V> type of values
   * @return hash map generator
   * @throws NullPointerException if klass or entry generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <K extends Enum<K>, V> Generator<EnumMap<K, V>> enumMapGen(Class<K> klass, Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, int maxTry) {
    checkNotNull(klass);
    return mutableMapGen(size -> new EnumMap<>(klass), entryGen, sizeGen, maxTry);
  }

  /**
   * Returns a new enum map generator from given entry generator and size generator.
   * The map implementation is {@link EnumMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see EnumMap
   * @see #enumMapGen(Class, Generator, Generator, int)
   *
   * @param klass Class of keys
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param <K> type of keys
   * @param <V> type of values
   * @return enum map generator
   * @throws NullPointerException if klass or entry generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <K extends Enum<K>, V> Generator<EnumMap<K, V>> enumMapGen(Class<K> klass, Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen) {
    return enumMapGen(klass, entryGen, sizeGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new enum set generator from given element generator and size generator.
   * If the generator isn't able to produce 1 new element in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see EnumSet
   * @see #enumSetGen(Class, Generator, Generator)
   *
   * @param klass Class of elements
   * @param elementGen Generator of element to put in set
   * @param sizeGen Generator of size of set
   * @param maxTry Maximum try to yield a new element
   * @param <T> type of elements
   * @return enum set generator
   * @throws NullPointerException if element generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <T extends Enum<T>> Generator<EnumSet<T>> enumSetGen(Class<T> klass, Generator<T> elementGen, Generator<Integer> sizeGen, int maxTry) {
    return Generators.map(hashSetGen(elementGen, sizeGen, maxTry), set -> set.isEmpty() ? EnumSet.noneOf(klass) : EnumSet.copyOf(set));
  }

  /**
   * Returns a new enum set generator from given element generator and size generator.
   * If the generator isn't able to produce 1 new element in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see EnumSet
   * @see #enumSetGen(Class, Generator, Generator, int)
   *
   * @param klass Class of elements
   * @param elementGen Generator of element to put in set
   * @param sizeGen Generator of size of set
   * @param <T> type of elements
   * @return enum set generator
   * @throws NullPointerException if element generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <T extends Enum<T>> Generator<EnumSet<T>> enumSetGen(Class<T> klass, Generator<T> elementGen, Generator<Integer> sizeGen) {
    return enumSetGen(klass, elementGen, sizeGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new hash map generator from given entry generator and size generator.
   * The map implementation is {@link HashMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see HashMap
   * @see #hashMapGen(Generator, Generator)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param maxTry Maximum try to yield a new key
   * @param <K> type of keys
   * @param <V> type of values
   * @return hash map generator
   * @throws NullPointerException if entry generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <K, V> Generator<HashMap<K, V>> hashMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableMapGen(HashMap::new, entryGen, sizeGen, maxTry);
  }

  /**
   * Returns a new hash map generator from given entry generator and size generator.
   * The map implementation is {@link HashMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see HashMap
   * @see #hashMapGen(Generator, Generator, int)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param <K> type of keys
   * @param <V> type of values
   * @return hash map generator
   * @throws NullPointerException if entry generator or size generator are null
   */
  @Gen
  public static <K, V> Generator<HashMap<K, V>> hashMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen) {
    return hashMapGen(entryGen, sizeGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new hash set generator from given element generator and size generator.
   * The set implementation is {@link HashSet}, so generated values may be null.
   * If the generator isn't able to produce 1 new element in a row of {@code maxSize},
   * then the generation is stopped and the set is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see HashSet
   * @see #hashSetGen(Generator, Generator)
   *
   * @param elementGen Generator of entries to add in set
   * @param sizeGen Generator of size of set
   * @param maxTry Maximum try to yield a new element
   * @param <T> type of elements
   * @return hash set generator
   * @throws NullPointerException if element generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <T> Generator<HashSet<T>> hashSetGen(Generator<T> elementGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableSetGen(HashSet::new, elementGen, sizeGen, maxTry);
  }

  /**
   * Returns a new hash set generator from given element generator and size generator.
   * The set implementation is {@link HashSet}, so generated values may be null.
   * If the generator isn't able to produce 1 new element in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the set is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see HashSet
   * @see #hashSetGen(Generator, Generator, int)
   *
   * @param elementGen Generator of entries to add in set
   * @param sizeGen Generator of size of set
   * @param <T> type of elements
   * @return hash set generator
   * @throws NullPointerException if element generator or size generator are null
   */
  @Gen
  public static <T> Generator<HashSet<T>> hashSetGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return mutableSetGen(HashSet::new, elementGen, sizeGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new identity hash map generator from given entry generator and size generator.
   * The map implementation is {@link IdentityHashMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see IdentityHashMap
   * @see #identityHashMapGen(Generator, Generator)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param maxTry Maximum try to yield a new key
   * @param <K> type of keys
   * @param <V> type of values
   * @return identity hash map generator
   * @throws NullPointerException if entry generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <K, V> Generator<IdentityHashMap<K, V>> identityHashMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableMapGen(IdentityHashMap::new, entryGen, sizeGen, maxTry);
  }

  /**
   * Returns a new identity hash map generator from given entry generator and size generator.
   * The map implementation is {@link IdentityHashMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see IdentityHashMap
   * @see #identityHashMapGen(Generator, Generator, int)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param <K> type of keys
   * @param <V> type of values
   * @return identity hash map generator
   * @throws NullPointerException if entry generator or size generator are null
   */
  @Gen
  public static <K, V> Generator<IdentityHashMap<K, V>> identityHashMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen) {
    return identityHashMapGen(entryGen, sizeGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new linked hash map generator from given entry generator and size generator.
   * The map implementation is {@link LinkedHashMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see LinkedHashMap
   * @see #linkedHashMapGen(Generator, Generator)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param maxTry Maximum try to yield a new key
   * @param <K> type of keys
   * @param <V> type of values
   * @return linked hash map generator
   * @throws NullPointerException if entry generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <K, V> Generator<LinkedHashMap<K, V>> linkedHashMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableMapGen(LinkedHashMap::new, entryGen, sizeGen, maxTry);
  }

  /**
   * Returns a new linked hash map generator from given entry generator and size generator.
   * The map implementation is {@link LinkedHashMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see LinkedHashMap
   * @see #linkedHashMapGen(Generator, Generator, int)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param <K> type of keys
   * @param <V> type of values
   * @return linked hash map generator
   * @throws NullPointerException if entry generator or size generator are null
   */
  @Gen
  public static <K, V> Generator<LinkedHashMap<K, V>> linkedHashMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen) {
    return linkedHashMapGen(entryGen, sizeGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new linked hash set generator from given element generator and size generator.
   * The set implementation is {@link LinkedHashSet}, so generated values may be null.
   * If the generator isn't able to produce 1 new element in a row of {@code maxSize},
   * then the generation is stopped and the set is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see LinkedHashSet
   * @see #linkedHashSetGen(Generator, Generator)
   *
   * @param elementGen Generator of entries to add in set
   * @param sizeGen Generator of size of set
   * @param maxTry Maximum try to yield a new element
   * @param <T> type of elements
   * @return linked hash set generator
   * @throws NullPointerException if element generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <T> Generator<LinkedHashSet<T>> linkedHashSetGen(Generator<T> elementGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableSetGen(LinkedHashSet::new, elementGen, sizeGen, maxTry);
  }

  /**
   * Returns a new linked hash set generator from given element generator and size generator.
   * The set implementation is {@link LinkedHashSet}, so generated values may be null.
   * If the generator isn't able to produce 1 new element in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the set is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see LinkedHashSet
   * @see #linkedHashSetGen(Generator, Generator, int)
   *
   * @param elementGen Generator of entries to add in set
   * @param sizeGen Generator of size of set
   * @param <T> type of elements
   * @return linked hash set generator
   * @throws NullPointerException if element generator or size generator are null
   */
  @Gen
  public static <T> Generator<LinkedHashSet<T>> linkedHashSetGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return linkedHashSetGen(elementGen, sizeGen, DEFAULT_MAX_TRY);
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
  @Gen
  public static <T> Generator<LinkedList<T>> linkedListGen(Generator<T> elementGen, Generator<Integer> sizeGen) {
    return collectedGen(elementGen, sizeGen, Collectors.toCollection(LinkedList::new));
  }

  /**
   * Returns a new locale generator from language codes defined in ISO 639 and from country codes defined in ISO 3166.
   *
   * @see Locale#getISOLanguages()
   * @see Locale#getISOCountries()
   *
   * @return locale generator
   */
  @Gen
  public static Generator<Locale> isoLocaleGen() {
    List<Locale> locales = Streams.zip(Arrays.stream(Locale.getISOLanguages()), Arrays.stream(Locale.getISOCountries()), Locale::new)
        .collect(Collectors.toList());
    return Generators.oneOf(locales);
  }

  /**
   * Returns a new priority queue generator from elements, size and comparator.
   *
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @see PriorityQueue
   *
   * @param elementGen Generator of elements
   * @param sizeGen Generator of size
   * @param comparatorGen Generator of comparator
   * @param <T> Type of elements
   * @return priority queue generator
   * @throws NullPointerException if any given generator is null
   *
   */
  @Gen
  public static <T> Generator<PriorityQueue<T>> priorityQueueGen(Generator<T> elementGen, Generator<Integer> sizeGen, Generator<Comparator<? super T>> comparatorGen) {
    checkNotNull(comparatorGen);
    Generator<ArrayList<T>> listGen = arrayListGen(elementGen, sizeGen);
    return re -> {
      PriorityQueue<T> queue = new PriorityQueue<>(comparatorGen.get(re));
      queue.addAll(listGen.get(re));
      return queue;
    };
  }

  /**
   * Returns new random generator from seed
   *
   * @param seedGen Generator of seed
   * @return random generator
   * @throws NullPointerException if seed generator if null
   */
  @Gen
  public static Generator<Random> randomGen(Generator<Long> seedGen) {
    return Generators.map(seedGen, Random::new);
  }

  /**
   * Returns a simple entry generator from given key and value generators.
   * Yielded keys and values may be null.
   *
   * @param keyGen Generator of keys
   * @param valueGen Generator of values
   * @param <K> type of keys
   * @param <V> type of values
   * @return simple entry generator
   * @throws NullPointerException if key or value generators are null
   */
  @Gen
  public static <K, V> Generator<AbstractMap.SimpleEntry<K, V>> simpleEntryGen(Generator<K> keyGen, Generator<V> valueGen) {
    checkNotNull(keyGen);
    checkNotNull(valueGen);
    return re -> new AbstractMap.SimpleEntry<>(keyGen.get(re), valueGen.get(re));
  }

  @Gen
  public static Generator<TimeZone> availableTimeZoneGen() {
    List<TimeZone> timeZones = Arrays.stream(TimeZone.getAvailableIDs())
        .map(TimeZone::getTimeZone)
        .collect(Collectors.toList());
    return Generators.oneOf(timeZones);
  }

  public static Generator<TimeZone> allTimeZoneGen() {
    List<TimeZone> timeZones = IntStream.rangeClosed(0, 24)
        .map(i -> i - 12)
        .mapToObj(i -> "GMT" + i + ":00")
        .map(TimeZone::getTimeZone)
        .collect(Collectors.toList());
    return Generators.oneOf(timeZones);
  }

  /**
   * Returns a new tree map generator from given entry generator and size generator.
   * The map implementation is {@link TreeMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@code maxSize},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see TreeMap
   * @see #treeMapGen(Generator, Generator, Generator)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param comparatorGen Generator of comparator
   * @param maxTry Maximum try to yield a new key
   * @param <K> type of keys
   * @param <V> type of values
   * @return tree map generator
   * @throws NullPointerException if entry generator, size generator or comparator generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <K, V> Generator<TreeMap<K, V>> treeMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, Generator<Comparator<? super K>> comparatorGen, int maxTry) {
    checkNotNull(entryGen);
    checkNotNull(sizeGen);
    checkNotNull(comparatorGen);
    checkArgument(maxTry >= 0);
    return re -> {
      Comparator<? super K> comparator = comparatorGen.get(re);
      return mutableMapGen(i -> new TreeMap<>(comparator), entryGen, sizeGen, maxTry).get(re);
    };
  }

  /**
   * Returns a new tree map generator from given entry generator and size generator.
   * The map implementation is {@link TreeMap}, so generated entries may contains null keys and null values.
   * When the entry generator yield two entries with the same keys, the second value is kept.
   * If the generator isn't able to produce 1 new key in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the map is returned even if given size is not reached.
   *
   * The generator throws {@link NullPointerException} if a null entry is yielded
   * and {@link IllegalStateException} if a negative size is produced.
   *
   * @see TreeMap
   * @see #treeMapGen(Generator, Generator, Generator, int)
   *
   * @param entryGen Generator of entries to put in map
   * @param sizeGen Generator of size of map
   * @param comparatorGen Generator of comparator
   * @param <K> type of keys
   * @param <V> type of values
   * @return tree map generator
   * @throws NullPointerException if entry generator, size generator or comparator generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  @Gen
  public static <K, V> Generator<TreeMap<K, V>> treeMapGen(Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, Generator<Comparator<? super K>> comparatorGen) {
    return treeMapGen(entryGen, sizeGen, comparatorGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new tree set generator from given element generator and size generator.
   * The set implementation is {@link TreeSet}, so generated values may be null.
   * If the generator isn't able to produce 1 new element in a row of {@code maxSize},
   * then the generation is stopped and the set is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see TreeSet
   * @see #treeSetGen(Generator, Generator, Generator)
   *
   * @param elementGen Generator of entries to add in set
   * @param sizeGen Generator of size of set
   * @param comparatorGen Generator of comparator
   * @param maxTry Maximum try to yield a new element
   * @param <T> type of elements
   * @return tree set generator
   * @throws NullPointerException if element generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  public static <T> Generator<TreeSet<T>> treeSetGen(Generator<T> elementGen, Generator<Integer> sizeGen, Generator<Comparator<? super T>> comparatorGen, int maxTry) {
    checkNotNull(elementGen);
    checkNotNull(sizeGen);
    checkNotNull(comparatorGen);
    checkArgument(maxTry >= 0);
    return re -> {
      Comparator<? super T> comparator = comparatorGen.get(re);
      return mutableSetGen(i -> new TreeSet<>(comparator), elementGen, sizeGen, maxTry).get(re);
    };
  }

  /**
   * Returns a new tree set generator from given element generator and size generator.
   * The set implementation is {@link TreeSet}, so generated values may be null.
   * If the generator isn't able to produce 1 new element in a row of {@link #DEFAULT_MAX_TRY},
   * then the generation is stopped and the set is returned even if given size is not reached.
   *
   * The generator throws {@link IllegalStateException} if a negative size is produced.
   *
   * @see TreeSet
   * @see #treeSetGen(Generator, Generator, Generator, int)
   *
   * @param elementGen Generator of entries to add in set
   * @param sizeGen Generator of size of set
   * @param comparatorGen Generator of comparator
   * @param <T> type of elements
   * @return tree set generator
   * @throws NullPointerException if element generator or size generator are null
   * @throws IllegalArgumentException if maxTry is negative
   */
  @Gen
  public static <T> Generator<TreeSet<T>> treeSetGen(Generator<T> elementGen, Generator<Integer> sizeGen, Generator<Comparator<? super T>> comparatorGen) {
    return treeSetGen(elementGen, sizeGen, comparatorGen, DEFAULT_MAX_TRY);
  }

  /**
   * Returns a new random UUID generator built with random longs
   *
   * @param longGen Generator of long
   * @return uuid generator
   * @throws NullPointerException if long generator is null
   */
  @Gen
  public static Generator<UUID> randomUUIDGen(Generator<Long> longGen) {
    checkNotNull(longGen);
    return re -> new UUID(longGen.get(re), longGen.get(re));
  }

  public static Registry utilsRegistry() {
    return Registries.alternatives(
        Registries.forClass(JavaUtils.class),
        getHierarchyRegistry(),
        getEnumRegistry());
  }

  private static Registry getHierarchyRegistry() {
    return Registries.builder()
        .putDyn(Iterator.class, fromHierachy1(Iterable.class).andThen(optGen -> optGen.map(gen -> Generators.map(gen, Iterable::iterator))))
        .putDyn(ListIterator.class, fromHierachy1(List.class).andThen(optGen -> optGen.map(gen -> Generators.map(gen, List::listIterator))))
        .putDyn(Iterable.class, fromHierachy1(Collection.class))
        .putDyn(Collection.class, fromHierachy1(Set.class, List.class, Queue.class))
        .putDyn(Map.Entry.class, fromHierachy2(AbstractMap.SimpleEntry.class))
        .putDyn(Map.class, fromHierachy2(HashMap.class, EnumMap.class, LinkedHashMap.class, SortedMap.class))
        .putDyn(SortedMap.class, fromHierachy2(NavigableMap.class))
        .putDyn(NavigableMap.class, fromHierachy2(TreeMap.class))
        .putDyn(Set.class, fromHierachy1(HashSet.class, EnumSet.class, LinkedHashSet.class, SortedSet.class))
        .putDyn(SortedSet.class, fromHierachy1(NavigableSet.class))
        .putDyn(NavigableSet.class, fromHierachy1(TreeSet.class))
        .putDyn(List.class, fromHierachy1(ArrayList.class, LinkedList.class))
        .putDyn(Deque.class, fromHierachy1(ArrayDeque.class))
        .putDyn(Queue.class, fromHierachy1(PriorityQueue.class, Deque.class))
        .build();
  }

  private static Registry getEnumRegistry() {
    return Registries.builder()
        .putDyn(EnumMap.class, (registry, keyId, valueId) -> {
          Class<?> keyClass = keyId.getTypeClass();
          if (!Enum.class.isAssignableFrom(keyClass)) {
            return Optional.empty();
          }
          Class<Enum> enumKeyClass = (Class<Enum>) keyClass;
          return (Optional) registry.lookup(classId(int.class)).flatMap(sizeGen ->
              registry.lookup(paramId(Map.Entry.class, keyId, valueId)).map(entryGen -> {
                Generator<Map.Entry<Enum, Object>> castedEntryGen = (Generator) entryGen;
                return JavaUtils.enumMapGen(enumKeyClass, castedEntryGen, sizeGen);
              }));
        })
        .putDyn(EnumSet.class, (registry, elementId) -> {
          Class<?> elemClass = elementId.getTypeClass();
          if (!Enum.class.isAssignableFrom(elemClass)) {
            return Optional.empty();
          }
          Class<Enum> enumElemClass = (Class<Enum>) elemClass;
          return registry.lookup(classId(int.class)).flatMap(sizeGen ->
              registry.lookup(elementId)
                  .map(elementGen -> (Generator<Enum>) (Generator) elementGen)
                  .map(elementGen -> JavaUtils.enumSetGen(enumElemClass, elementGen, sizeGen)));
        })
        .build();
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

  private static <K, V, M extends Map<K, V>> Generator<M> mutableMapGen(IntFunction<M> factory, Generator<? extends Map.Entry<K, V>> entryGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableKeyBasedCollectionGen(factory, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::size, entryGen, sizeGen, maxTry);
  }

  private static <T, S extends Set<T>> Generator<S> mutableSetGen(IntFunction<S> factory, Generator<T> elementGen, Generator<Integer> sizeGen, int maxTry) {
    return mutableKeyBasedCollectionGen(factory, Set::add, Set::size, elementGen, sizeGen, maxTry);
  }

  /**
   * Returns a new element from collected finite stream generator.
   *
   * If the size generator yield a negative number, an {@link IllegalStateException} is thrown.
   *
   * @param elementGen generator of elements in list
   * @param sizeGen size generator
   * @param collector collector to build list
   * @param <T> type of element in list
   * @param <L> type of the collected result
   * @return collected list generator
   * @throws NullPointerException if any parameters is null
   */
  public static <T, L> Generator<L> collectedGen(Generator<T> elementGen, Generator<Integer> sizeGen, Collector<T, ?, L> collector) {
    checkNotNull(collector);
    return Generators.map(StreamGens.finiteGen(elementGen, intToLong(sizeGen)), streamGen -> streamGen.collect(collector));
  }

  private static <T, C> Generator<C> mutableKeyBasedCollectionGen(IntFunction<C> factory, BiConsumer<C, T> mutator, ToIntFunction<C> sizeF, Generator<T> valueGen, Generator<Integer> sizeGen, int maxTry) {
    checkNotNull(valueGen);
    checkNotNull(sizeGen);
    checkArgument(maxTry >= 0);
    return re -> {
      int size = sizeGen.get(re);
      checkState(size >= 0);

      C collection = factory.apply(size);

      int nbFail = 0;
      while (sizeF.applyAsInt(collection) < size) {
        int currentSize = sizeF.applyAsInt(collection);
        mutator.accept(collection, valueGen.get(re));

        if (currentSize == sizeF.applyAsInt(collection)) {
          nbFail++;
          if (nbFail >= maxTry) {
            break;
          }
        } else {
          nbFail = 0;
        }
      }
      return collection;
    };
  }
}
