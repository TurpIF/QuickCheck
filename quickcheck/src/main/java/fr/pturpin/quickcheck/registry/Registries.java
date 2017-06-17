package fr.pturpin.quickcheck.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.base.Optionals;
import fr.pturpin.quickcheck.base.Reflections;
import fr.pturpin.quickcheck.functional.Checked.CheckedSupplier;
import fr.pturpin.quickcheck.functional.Function3;
import fr.pturpin.quickcheck.functional.Function4;
import fr.pturpin.quickcheck.functional.Function5;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static fr.pturpin.quickcheck.functional.Checked.CheckedFunction.unchecked;
import static fr.pturpin.quickcheck.identifier.Identifiers.typeId;

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


  public static Registry forClass(Class<?> klass) {
    RegistryBuilder builder = builder();

    Arrays.stream(klass.getDeclaredMethods())
        .filter(method -> method.getAnnotation(Gen.class) != null)
        .map(Registries::getIdentifiedGenerator)
        .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));

    Registry registry = builder.build();
    Class<?> superclass = klass.getSuperclass();
    return superclass == null ? registry : alternatives(registry, forClass(superclass));
  }

  private static Entry<TypeIdentifier<Object>, Function<Registry, Optional<Generator<Object>>>> getIdentifiedGenerator(Method method) {
    checkState(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()),
        "@Gen method should be public and static");

    Parameter[] parameters = method.getParameters();
    checkState(parameters.length == 0 || parameters.length == 1 && parameters[0].getType().equals(Registry.class),
        "@Gen method should either have no parameter or have one Registry parameter");

    TypeIdentifier<Object> returnId = typeId(method.getGenericReturnType());
    TypeIdentifier<?> generatorId;
    Consumer<Boolean> checker;
    Function<Registry, Optional<Generator<?>>> generatorF;

    if (parameters.length == 1) {
      checker = state -> checkState(state, "@Gen method should return Optional<Generator<T>> when getting Registry as parameter but was %s for method %s", returnId, method);

      checker.accept(returnId.getTypeClass().equals(Optional.class)
          && returnId.getNbParametrizedType() == 1
          && returnId.getParametrizedType().isPresent());

      generatorId = returnId.getParametrizedType().get().get(0);

      generatorF = unchecked(Reflections.<Registry>invoker1(method).andThen(obj -> (Optional<Generator<?>>) obj));
    } else {
      checker = state -> checkState(state, "@Gen method should return Generator<T> for method %s", method);
      generatorId = returnId;

      CheckedSupplier<Object, ?> rawGetter = Reflections.<Registry>invoker0(method);
      generatorF = unchecked(registry -> Optional.of((Generator<?>) rawGetter.get()));
    }

    checker.accept(generatorId.getTypeClass().equals(Generator.class)
        && generatorId.getNbParametrizedType() == 1
        && generatorId.getParametrizedType().isPresent());

    TypeIdentifier<?> elementId = generatorId.getParametrizedType().get().get(0);
    return Maps.immutableEntry((TypeIdentifier) elementId, (Function) generatorF);
  }

  public static Registry empty() {
    return EmptyRegistry.EMPTY_INSTANCE;
  }

  public static Registry alternatives(Iterable<Registry> registries) {
    ImmutableList<Registry> list = Streams.stream(registries)
        .filter(re -> re != EmptyRegistry.EMPTY_INSTANCE)
        .collect(toImmutableList());
    return list.isEmpty() ? empty() : list.size() == 1 ? list.get(0) : new AlternativeRegistry(list);
  }

  public static Registry alternatives(Registry... registries) {
    return alternatives(Arrays.asList(registries));
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

  public static final class StaticRegistry implements Registry {
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

    public static <A, T> Function<Registry, Optional<Generator<T>>> resolved(TypeIdentifier<A> firstType, Function<Generator<A>, Generator<T>> mapper) {
      return (Registry registry) -> registry.lookup(firstType).map(mapper);
    }
  }

  public static final class DynamicRegistry implements Registry {
    private final ImmutableMap<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> map;

    private DynamicRegistry(ImmutableMap<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> map) {
      this.map = checkNotNull(map);
    }

    @Override
    public <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier) {
      return identifier.getParametrizedType().flatMap(parameters -> {
        Optional<Generator<?>> generator = Optional.ofNullable(map.get(identifier.getTypeClass()))
            .flatMap(f -> f.apply(root, parameters));
        return generator.map(Generator.class::cast);
      });
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
      ImmutableMap<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> staticMap = staticBuilder.build();
      ImmutableMap<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> dynamicMap = dynamicBuilder.build();

      if (staticMap.isEmpty() && dynamicMap.isEmpty()) {
        return empty();
      } else if (staticMap.isEmpty()) {
        return new DynamicRegistry(dynamicMap);
      } else if (dynamicMap.isEmpty()) {
        return new StaticRegistry(staticMap);
      } else {
        return alternatives(
            new StaticRegistry(staticMap),
            new DynamicRegistry(dynamicMap));
      }
    }
  }
}
