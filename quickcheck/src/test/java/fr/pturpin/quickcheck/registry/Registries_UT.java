package fr.pturpin.quickcheck.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.identifier.ParametrizedIdentifier;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registries.RegistryBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fr.pturpin.quickcheck.generator.Generators.constGen;
import static fr.pturpin.quickcheck.generator.Generators.oneOf;
import static fr.pturpin.quickcheck.identifier.Identifiers.classId;
import static fr.pturpin.quickcheck.identifier.Identifiers.paramId;
import static fr.pturpin.quickcheck.registry.Registries.DynamicRegistry.resolved;
import static fr.pturpin.quickcheck.registry.Registries.alternatives;

/**
 * Created by pturpin on 06/06/2017.
 */
public class Registries_UT {

  public static final Set<TypeIdentifier<?>> IDENTIFIERS = ImmutableSet.of(
      classId(String.class), classId(void.class), classId(int.class),
      classId(List.class), classId(Test.class));

  @Test
  public void forMapShouldReproduceContentsOfGivenMap() {
    forMapShouldReproduceContentsOfGivenMap(ImmutableMap.of());
    forMapShouldReproduceContentsOfGivenMap(ImmutableMap.of(classId(String.class), constGen("")));
    forMapShouldReproduceContentsOfGivenMap(ImmutableMap.of(classId(Integer.class), constGen(1)));
    forMapShouldReproduceContentsOfGivenMap(ImmutableMap.of(
        classId(String.class), constGen(""),
        classId(Integer.class), constGen(1)));
  }

  private static void forMapShouldReproduceContentsOfGivenMap(Map<TypeIdentifier<?>, Generator<?>> map) {
    Registry registry = Registries.forMap(map);

    map.forEach((id, generator) -> {
      Optional<Generator<?>> optFetchedGen = registry.lookup((TypeIdentifier) id);
      Assert.assertTrue(optFetchedGen.isPresent());
      Generator<?> fetchedGen = optFetchedGen.get();
      Assert.assertEquals(generator, fetchedGen);
    });

    IDENTIFIERS.forEach(id -> {
      Generator<?> inMapGen = map.get(id);
      Optional<Generator<?>> optFetchedGen = registry.lookup((TypeIdentifier) id);

      Assert.assertEquals(inMapGen != null, optFetchedGen.isPresent());
      optFetchedGen.ifPresent(fetchedGen -> Assert.assertEquals(inMapGen, fetchedGen));
    });
  }

  @Test
  public void emptyRegistryShouldAlwaysProduceEmpty() {
    Registry registry = Registries.empty();

    List<TypeIdentifier<?>> identifiers = ImmutableList.of(
        classId(String.class), classId(void.class), classId(int.class),
        classId(List.class), classId(Test.class));

    identifiers.forEach(id -> Assert.assertFalse(registry.lookup(id).isPresent()));
  }

  @Test
  public void alternativeOfDistinctSingletonShouldActLikeRegistryOfEntireMap() {
    alternativeOfDistinctSingletonShouldActLikeRegistryOfEntireMap(ImmutableMap.of(
        classId(String.class), constGen(""),
        classId(Integer.class), constGen(1)));
    alternativeOfDistinctSingletonShouldActLikeRegistryOfEntireMap(ImmutableMap.of(
        classId(Double.class), constGen(0.d),
        classId(Float.class), constGen(0.f)));
  }

  private static void alternativeOfDistinctSingletonShouldActLikeRegistryOfEntireMap(Map<TypeIdentifier<?>, Generator<?>> map) {
    List<Registry> singletonRegistries = map.entrySet().stream()
        .map(entry -> ImmutableMap.of(entry.getKey(), entry.getValue()))
        .map(Registries::forMap)
        .collect(toImmutableList());

    Registry alternative = alternatives(singletonRegistries);
    Registry mapRegistry = Registries.forMap(map);

    assertEqualsRegistries(Sets.union(IDENTIFIERS, map.keySet()), alternative, mapRegistry);
  }

  @Test
  public void alternativeShouldBeOrderedAndSelectFirstFetchGenerator() {
    Generator<String> helloGen = constGen("Hello");
    Generator<String> worldGen = constGen("World");
    Registry helloRegistry = Registries.forMap(ImmutableMap.of(classId(String.class), helloGen));
    Registry worldRegistry = Registries.forMap(ImmutableMap.of(classId(String.class), worldGen));

    Registry alternative = alternatives(helloRegistry, worldRegistry);

    Optional<Generator<String>> optGenerator = alternative.lookup(classId(String.class));
    Assert.assertTrue(optGenerator.isPresent());

    Generator<String> generator = optGenerator.get();
    Assert.assertEquals(helloGen, generator);
  }

  @Test
  public void builderWithStaticEntryShouldActLikeMapRegistryOfEntries() {
    builderWithStaticEntryShouldActLikeMapRegistryOfEntries(ImmutableMap.of());
    builderWithStaticEntryShouldActLikeMapRegistryOfEntries(ImmutableMap.of(classId(String.class), constGen("")));
    builderWithStaticEntryShouldActLikeMapRegistryOfEntries(ImmutableMap.of(classId(Integer.class), constGen(1)));
    builderWithStaticEntryShouldActLikeMapRegistryOfEntries(ImmutableMap.of(
        classId(String.class), constGen(""),
        classId(Integer.class), constGen(1)));
  }

  private static void builderWithStaticEntryShouldActLikeMapRegistryOfEntries(Map<TypeIdentifier<?>, Generator<?>> map) {
    RegistryBuilder builder = Registries.builder();
    map.forEach((id, generator) -> builder.put((TypeIdentifier) id, generator));
    Registry builtRegistry = builder.build();
    Registry mapRegistry = Registries.forMap(map);

    assertEqualsRegistries(Sets.union(IDENTIFIERS, map.keySet()), builtRegistry, mapRegistry);
  }

  @Test
  public void builderWithStaticFunctionalEntryShouldLookUpInsideRegistry() {
    Function<String, List<Character>> mapper = string -> IntStream.range(0, string.length())
        .mapToObj(string::charAt)
        .collect(Collectors.toList());

    Registry registry = Registries.builder()
        .put(classId(String.class), oneOf("Lorem", "ipsum", "dolor", "sit", "amet"))
        .put(paramId((Class) List.class, String.class), (Registry r) ->
            r.lookup(classId(String.class)).map(strGen -> Generators.map(strGen, mapper)))
        .build();

    Generator<String> strGen = registry.lookup(classId(String.class)).get();
    Generator<List> listGen = registry.lookup(paramId(List.class, String.class)).get();

    Random randomStr = new Random(0);
    Random randomList = new Random(0);

    for (int i = 0; i < 10; i++) {
      String s = strGen.get(randomStr);
      List list = listGen.get(randomList);
      Assert.assertEquals(mapper.apply(s), list);
    }
  }

  @Test
  public void buildWithDynamicEntryShouldLookupInsideRegistry() {
    Registry registry = Registries.builder()
        .put(classId(String.class), oneOf("Lorem", "ipsum", "dolor", "sit", "amet"))
        .putDyn((Class) List.class, resolved(elemGen -> Generators.map(elemGen, ImmutableList::of)))
        .build();

    Generator<String> strGen = registry.lookup(classId(String.class)).get();
    Generator<List> listGen = registry.lookup(paramId(List.class, String.class)).get();

    Random randomStr = new Random(0);
    Random randomList = new Random(0);

    for (int i = 0; i < 10; i++) {
      String s = strGen.get(randomStr);
      List list = listGen.get(randomList);
      Assert.assertEquals(ImmutableList.of(s), list);
    }
  }

  @Test
  public void buildWithDynamicEntryShouldRecognizedParametrizedId() {
    Registry registry = Registries.builder()
        .put(classId(String.class), oneOf("Lorem", "ipsum", "dolor", "sit", "amet"))
        .putDyn((Class) List.class, resolved(elemGen -> Generators.map(elemGen, ImmutableList::of)))
        .build();

    Assert.assertTrue(registry.lookup(paramId(List.class, String.class)).isPresent());
    Assert.assertTrue(registry.lookup(paramId(List.class, paramId(List.class, String.class))).isPresent());
    Assert.assertFalse(registry.lookup(paramId(List.class, Integer.class)).isPresent());
  }

  @Test
  public void buildWithStaticAndDynamicShouldLookUpFirstAtStaticThenDynamic() {
    Generator<List<String>> staticGen = constGen(ImmutableList.of("static"));
    Generator<List<String>> dynamicGen = constGen(ImmutableList.of());

    Registry registry = Registries.builder()
        .put(paramId((Class) List.class, String.class), staticGen)
        .putDyn((Class) List.class, (r, id) -> Optional.of(dynamicGen))
        .build();

    Generator<List> listStrGen = registry.lookup(paramId(List.class, String.class)).get();
    Generator<List> listIntGen = registry.lookup(paramId(List.class, Integer.class)).get();

    Assert.assertEquals(staticGen, listStrGen);
    Assert.assertEquals(dynamicGen, listIntGen);
  }

  // re1[key1] != null & re2[dyn[T]] != null & re2[dyn[key1]] == null => re3[dyn[key1]] != null

  @Test
  public void registryShouldLookUpRecursively() {
    Registry empty = Registries.empty();
    Registry classRegistry = Registries.forMap(ImmutableMap.of(
        classId(String.class), constGen("Hello"),
        classId(Integer.class), oneOf(0, 1, 2, 3)));
    Registry supplierStrRegistry = Registries.forMap(ImmutableMap.of(
        paramId(Supplier.class, String.class), (Generator<Supplier<String>>) re -> () -> "World"));
    Registry supplierRegistry = Registries.builder()
        .putDyn(Supplier.class, resolved(gen -> Generators.<Object, Supplier>map(gen, v -> () -> v)))
        .build();

    {
      Registry stringAlt = alternatives(supplierRegistry, supplierStrRegistry, classRegistry);
      Optional<Generator<Supplier<String>>> optGen = stringAlt.<Supplier<String>>lookup(paramId((Class) Supplier.class, String.class));
      Assert.assertEquals(optGen.get().get(new Random()).get(), "Hello");
    }

    {
      Registry stringAlt = alternatives(supplierStrRegistry, supplierRegistry, classRegistry);
      Optional<Generator<Supplier<String>>> optGen = stringAlt.<Supplier<String>>lookup(paramId((Class) Supplier.class, String.class));
      Assert.assertEquals(optGen.get().get(new Random()).get(), "World");
    }

    IDENTIFIERS.forEach(id -> Assert.assertFalse(alternatives(empty, empty).lookup(id).isPresent()));
    IDENTIFIERS.forEach(id -> Assert.assertFalse(alternatives(empty, alternatives(empty, empty)).lookup(id).isPresent()));

    IDENTIFIERS.forEach(identifier -> {
      Arrays.asList(empty, classRegistry, supplierStrRegistry).forEach(registry -> {
        TypeIdentifier id = (TypeIdentifier) identifier;
        ParametrizedIdentifier<Supplier<?>> paramId = paramId((Class) Supplier.class, identifier);
        Optional<Generator> expected = registry.lookup(id);
        Optional<Generator<Supplier<?>>> paramExpected = registry.lookup(paramId);

        Registry simpleAlt = alternatives(registry, supplierRegistry);
        Registry complexAlt = alternatives(
            alternatives(empty, registry),
            alternatives(supplierRegistry, empty));
        Arrays.asList(simpleAlt, complexAlt).forEach(alternative -> {
          Optional<Generator<?>> actual = alternative.lookup(paramId).map(gen -> Generators.map(gen, Supplier::get));

          expected.ifPresent(expectedGen -> {
            Assert.assertTrue(actual.isPresent());
            Generator<?> actualGen = actual.get();

            Random expectedRandom = new Random(0);
            Random actualRandom = new Random(0);
            for (int i = 0; i < 10; i++) {
              Object fetchedExpected = expectedGen.get(expectedRandom);
              Object fetchedActual = actualGen.get(actualRandom);
              Assert.assertEquals(fetchedExpected, fetchedActual);
            }
          });

          paramExpected.ifPresent(p -> Assert.assertTrue(actual.isPresent()));
        });
      });
    });
  }

  private static void assertEqualsRegistries(Iterable<TypeIdentifier<?>> identifiers, Registry left, Registry right) {
    identifiers.forEach(id -> Assert.assertEquals(left.lookup(id), right.lookup(id)));
  }
}
