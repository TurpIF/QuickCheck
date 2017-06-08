package fr.pturpin.quickcheck.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import fr.pturpin.quickcheck.base.Optionals;
import fr.pturpin.quickcheck.functional.Function3;
import fr.pturpin.quickcheck.functional.Function4;
import fr.pturpin.quickcheck.functional.Function5;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.ParametrizedIdentifier;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Created by pturpin on 27/04/2017.
 */
public final class Registries {

  private Registries() { /* Helper class */ }

  public static RegistryBuilder builder() {
    return new RegistryBuilder();
  }

  public static Registry forMap(Map<? extends TypeIdentifier<?>, ? extends Generator<?>> map) {
    return new StaticRegistry(ImmutableMap.copyOf(Maps.transformValues(map, v -> r -> Optional.of(v))));
  }

  public static Registry empty() {
    return EmptyRegistry.EMPTY_INSTANCE;
  }

  public static Registry alternatives(Iterable<Registry> registries) {
    return new AlternativeRegistry(ImmutableList.copyOf(registries));
  }

  public static Registry alternatives(Registry... registries) {
    return new AlternativeRegistry(ImmutableList.copyOf(registries));
  }

  private static final class AlternativeRegistry implements Registry {
    private final ImmutableList<Registry> registries;

    private AlternativeRegistry(ImmutableList<Registry> registries) {
      checkArgument(registries.size() > 1);
      this.registries = registries;
    }

    @Override
    public <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier) {
      return registries.stream()
          .map(registry -> registry.recursiveLookup(root, identifier))
          .flatMap(Streams::stream)
          .findFirst();
    }
  }

  private static final class EmptyRegistry implements Registry {
    private static final Registry EMPTY_INSTANCE = new EmptyRegistry();

    EmptyRegistry() { /* nothing */ }

    @Override
    public <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier) {
      return Optional.empty();
    }
  }

  private static final class StaticRegistry implements Registry {
    private final ImmutableMap<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> map;

    private StaticRegistry(ImmutableMap<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> map) {
      this.map = checkNotNull(map);
    }

    @Override
    public <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier) {
      Optional<Generator<?>> generator = Optional.ofNullable(map.get(identifier))
          .flatMap(f -> f.apply(root));
      return generator.map(Generator.class::cast);
    }
  }

  public static final class DynamicRegistry implements Registry {
    private final ImmutableMap<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> map;

    private DynamicRegistry(ImmutableMap<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> map) {
      this.map = checkNotNull(map);
    }

    @Override
    public <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier) {
      if (!(identifier instanceof ParametrizedIdentifier)) {
        return Optional.empty();
      }

      List<TypeIdentifier<?>> parameters = ((ParametrizedIdentifier<T>) identifier).getParameters();
      Optional<Generator<?>> generator = Optional.ofNullable(map.get(identifier.getTypeClass()))
          .flatMap(f -> f.apply(root, parameters));
      return generator.map(Generator.class::cast);
    }

    public static <T> BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<T>>> allResolved(Function<List<Generator>, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, types) ->
          types.stream()
              .map(registry::lookup)
              .collect(Optionals.allPresent(toImmutableList()))
              .map(generators -> mapper.apply((List) generators));
    }

    public static <T> BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<T>>> resolved(Function<Generator, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType) ->
          registry.lookup(firstType)
              .map(mapper::apply);
    }

    public static <T> Function3<Registry, TypeIdentifier<?>, TypeIdentifier<?>, Optional<Generator<T>>> resolved(BiFunction<Generator, Generator, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType, secondType) ->
          registry.lookup(firstType)
              .flatMap(firstGen -> registry.lookup(secondType)
                  .map(secondGen -> mapper.apply(firstGen, secondGen)));
    }

    public static <T> Function4<Registry, TypeIdentifier<?>, TypeIdentifier<?>, TypeIdentifier<?>, Optional<Generator<T>>> resolved(Function3<Generator, Generator, Generator, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType, secondType, thirdType) ->
          registry.lookup(firstType)
              .flatMap(firstGen -> registry.lookup(secondType)
                  .flatMap(secondGen -> registry.lookup(thirdType)
                      .map(thirdGen -> mapper.apply( firstGen, secondGen, thirdGen))));
    }

    public static <T> Function5<Registry, TypeIdentifier<?>, TypeIdentifier<?>, TypeIdentifier<?>, TypeIdentifier<?>, Optional<Generator<T>>> resolved(Function4<Generator, Generator, Generator, Generator, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType, secondType, thirdType, forthType) ->
          registry.lookup(firstType)
              .flatMap(firstGen -> registry.lookup(secondType)
                  .flatMap(secondGen -> registry.lookup(thirdType)
                      .flatMap(thirdGen -> registry.lookup(forthType)
                          .map(forthGen -> mapper.apply( firstGen, secondGen, thirdGen, forthGen)))));
    }
  }

  public static final class RegistryBuilder {
    private final ImmutableMap.Builder<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> staticBuilder;
    private final ImmutableMap.Builder<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> dynamicBuilder;

    private RegistryBuilder() {
      this.staticBuilder = ImmutableMap.builder();
      this.dynamicBuilder = ImmutableMap.builder();
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, Generator<T> generator) {
      staticBuilder.put(identifier, r -> Optional.of(generator));
      return this;
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, Function<Registry, Optional<Generator<T>>> generatorFactory) {
      staticBuilder.put(identifier, (Function) generatorFactory);
      return this;
    }

    public <T> RegistryBuilder putDyn(Class<T> identifier, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 1 ? generatorFactory.apply(registry, params.get(0)) : Optional.empty());
    }

    public <T> RegistryBuilder putDyn(Class<T> identifier, Function3<Registry, TypeIdentifier<?>, TypeIdentifier<?>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 2 ? generatorFactory.apply(registry, params.get(0), params.get(1)) : Optional.empty());
    }

    public <T> RegistryBuilder putDyn(Class<T> identifier, Function4<Registry, TypeIdentifier<?>, TypeIdentifier<?>, TypeIdentifier<?>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 3 ? generatorFactory.apply(registry, params.get(0), params.get(1), params.get(2)) : Optional.empty());
    }

    public <T> RegistryBuilder putDyn(Class<T> identifier, Function5<Registry, TypeIdentifier<?>, TypeIdentifier<?>, TypeIdentifier<?>, TypeIdentifier<?>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 4 ? generatorFactory.apply(registry, params.get(0), params.get(1), params.get(2), params.get(3)) : Optional.empty());
    }

    public <T> RegistryBuilder putDynamic(Class<T> identifier, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<T>>> generatorFactory) {
      dynamicBuilder.put(identifier, (BiFunction) generatorFactory);
      return this;
    }

    public Registry build() {
      return alternatives(
          new StaticRegistry(staticBuilder.build()),
          new DynamicRegistry(dynamicBuilder.build()));
    }
  }
}
