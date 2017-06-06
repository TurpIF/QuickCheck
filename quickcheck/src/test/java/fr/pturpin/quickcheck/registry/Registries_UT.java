package fr.pturpin.quickcheck.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fr.pturpin.quickcheck.generator.Generators.constGen;
import static fr.pturpin.quickcheck.identifier.ClassIdentifier.classId;

/**
 * Created by pturpin on 06/06/2017.
 */
public class Registries_UT {

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

    List<TypeIdentifier<?>> identifiers = ImmutableList.of(
        classId(String.class), classId(void.class), classId(int.class),
        classId(List.class), classId(Test.class));

    identifiers.forEach(id -> {
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

    Registry alternative = Registries.alternatives(singletonRegistries);
    Registry mapRegistry = Registries.forMap(map);

    List<TypeIdentifier<?>> identifiers = ImmutableList.of(
        classId(String.class), classId(void.class), classId(int.class),
        classId(List.class), classId(Test.class));

    Stream.concat(identifiers.stream(), map.keySet().stream())
      .forEach(id -> Assert.assertEquals(alternative.lookup(id), mapRegistry.lookup(id)));
  }

  @Test
  public void alternativeShouldBeOrderedAndSelectFirstFetchGenerator() {
    Generator<String> helloGen = constGen("Hello");
    Generator<String> worldGen = constGen("World");
    Registry helloRegistry = Registries.forMap(ImmutableMap.of(classId(String.class), helloGen));
    Registry worldRegistry = Registries.forMap(ImmutableMap.of(classId(String.class), worldGen));

    Registry alternative = Registries.alternatives(helloRegistry, worldRegistry);

    Optional<Generator<String>> optGenerator = alternative.lookup(classId(String.class));
    Assert.assertTrue(optGenerator.isPresent());

    Generator<String> generator = optGenerator.get();
    Assert.assertEquals(helloGen, generator);
  }
}
