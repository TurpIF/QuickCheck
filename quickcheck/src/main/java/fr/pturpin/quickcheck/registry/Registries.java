package fr.pturpin.quickcheck.registry;

import com.google.common.collect.*;
import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.base.Optionals;
import fr.pturpin.quickcheck.base.Reflections;
import fr.pturpin.quickcheck.functional.Function3;
import fr.pturpin.quickcheck.functional.Function4;
import fr.pturpin.quickcheck.functional.Function5;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.Identifiers;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.identifier.WildcardIdentifier;

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
import java.util.function.UnaryOperator;

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

  /**
   * Build a new static registry from the given map.
   *
   * @param map association between identifier and generators
   * @return static registry
   * @throws NullPointerException if map is null
   */
  public static Registry forMap(Map<? extends TypeIdentifier<?>, ? extends Generator<?>> map) {
    return new StaticRegistry(ImmutableMap.copyOf(Maps.transformValues(map, v -> (r, i) -> Optional.of(v))));
  }

  /**
   * Build a singleton registry from given method.
   *
   * The given method should follow some rules to be considered as a valid generator supplier :
   * <ul>
   *   <li>The method should be {@code public} and {@code static}</li>
   *   <li>It may have one {@link Registry} parameters.
   *   Then it should return {@code Optional<Generator<T>>} for any static type {@code T}</li>
   *   <li>It may have no parameters.
   *   Then it should return {@code Generator<T>} for any static type {@code T}</li>
   *   <li>It may have many {@link Generator} parameters.
   *   Then it should return {@code Generator<T>} for any static or generic type {@code T}.
   *   The resulting generator may be used only if all needed generator had been looked up in the registry.</li>
   * </ul>
   *
   * If the method does not follow these rules, a {@link IllegalStateException} is thrown.
   *
   * @param method method to inspect
   * @return singleton registry
   * @throws NullPointerException if method is null
   * @throws IllegalStateException if method doesn't follow appropriate rules
   */
  public static Registry forMethod(Method method) {
    Entry<TypeIdentifier<Object>, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<Object>>>> identifiedGen = getIdentifiedGenerator(method);
    return Registries.builder()
        .put(identifiedGen.getKey(), identifiedGen.getValue())
        .build();
  }

  /**
   * Build a registry from the given class by using all generator methods that are annotated by {@link Gen}.
   * <p>
   * The acceptable rules for each method to be considered as a valid generator method
   * is described in {@link #forMethod(Method)}.
   * <p>
   * If many methods produce the same generator, an exception is thrown.
   *
   * @see #forMethod(Method)
   * @see Gen
   *
   * @param klass class to inspect
   * @return registry of generator methods in the class
   * @throws NullPointerException if class is null
   * @throws RuntimeException if more than one method yield generators for the same type identifier
   */
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

  private static Entry<TypeIdentifier<Object>, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<Object>>>> getIdentifiedGenerator(Method method) {
    checkNotNull(method);
    checkState(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()),
        "@Gen method should be public and static");

    Parameter[] parameters = method.getParameters();

    TypeIdentifier<Object> returnId = typeId(method.getGenericReturnType());
    TypeIdentifier<?> generatorId;
    Consumer<Boolean> checker;
    BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<Object>>> generatorF;

    if (parameters.length == 1 && Registry.class.equals(parameters[0].getType())) {
      checker = state -> checkState(state, "@Gen method should return Optional<Generator<T>> when getting Registry as parameter but was %s for method %s", returnId, method);

      checker.accept(returnId.getTypeClass().equals(Optional.class)
          && returnId.getNbParametrizedType() == 1
          && returnId.getParametrizedType().isPresent());

      generatorId = returnId.getParametrizedType().get().get(0);

      Function<Registry, Optional<Generator<Object>>> function = unchecked(Reflections.invoker(method))
          .compose((Registry r) -> new Object[]{r})
          .andThen(obj -> (Optional<Generator<Object>>) obj);
      generatorF = (registry, ids) -> function.apply(registry);
    } else {
      checker = state -> checkState(state, "@Gen method should return Generator<T> for method %s and only accept Generators as parameters", method);
      generatorId = returnId;

      List<TypeIdentifier<Object>> parameterIds = Arrays.stream(parameters)
          .map(p -> typeId(p.getParameterizedType()))
          .collect(toImmutableList());

      checker.accept(Iterables.all(parameterIds, id -> Generator.class.equals(id.getTypeClass())
          && id.getNbParametrizedType() == 1
          && id.getParametrizedType().isPresent()));

      List<TypeIdentifier<?>> neededRawIds = parameterIds.stream()
          .map(id -> id.getParametrizedType().get().get(0))
          .collect(toImmutableList());

      BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<Object>>> lookUp = DynamicRegistry.allResolved(generators -> {
        Function<Object[], Generator<Object>> invoker = unchecked(Reflections.invoker(method))
            .andThen(obj -> (Generator<Object>) obj);
        return invoker.apply(generators.toArray());
      });

      generatorF = (registry, ids) -> {
        TypeIdentifier<Object> rawReturnId = (TypeIdentifier) generatorId.getParametrizedType().get().get(0);
        TypeIdentifier<Object> runtimeReturnId = (TypeIdentifier) ids;
        Map<WildcardIdentifier, TypeIdentifier<?>> wilcardMap = Identifiers.fetchWilcardResolution(rawReturnId, runtimeReturnId);
        UnaryOperator<TypeIdentifier<?>> replacer = neededId -> Identifiers.replace(wilcardMap, neededId);
        ImmutableList<TypeIdentifier<?>> neededIds = neededRawIds.stream().map(replacer).collect(toImmutableList());
        return lookUp.apply(registry, neededIds);
      };
    }

    checker.accept(generatorId.getTypeClass().equals(Generator.class)
        && generatorId.getNbParametrizedType() == 1
        && generatorId.getParametrizedType().isPresent());

    TypeIdentifier<Object> elementId = (TypeIdentifier) generatorId.getParametrizedType().get().get(0);
    return Maps.immutableEntry(elementId, generatorF);
  }

  /**
   * Returns an empty registry that always return empty.
   *
   * @return empty registry
   */
  public static Registry empty() {
    return EmptyRegistry.EMPTY_INSTANCE;
  }

  /**
   * Returns an alternative registry between all given one.
   * When looking up in the alternative, it looks, in order, in all sub registries and return the first non-empty found generator.
   *
   * If no registries are given, it acts like the {@link #empty()} registry.
   * If only one registry is given, it acts like the given registry itself.
   *
   * @param registries sub registries
   * @return alternative between given registries
   * @throws NullPointerException if given iterable is null or one registry is null
   */
  public static Registry alternatives(Iterable<Registry> registries) {
    ImmutableList<Registry> list = Streams.stream(registries)
        .filter(re -> re != EmptyRegistry.EMPTY_INSTANCE)
        .collect(toImmutableList());
    return list.isEmpty() ? empty() : list.size() == 1 ? list.get(0) : new AlternativeRegistry(list);
  }

  /**
   * Returns an alternative registry between all given one.
   * When looking up in the alternative, it looks, in order, in all sub registries and return the first non-empty found generator.
   *
   * If no registries are given, it acts like the {@link #empty()} registry.
   * If only one registry is given, it acts like the given registry itself.
   *
   * @see #alternatives(Iterable)
   *
   * @param registries sub registries
   * @return alternative between given registries
   * @throws NullPointerException if given array is null or one registry is null
   */
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
    private final ImmutableMap<TypeIdentifier<?>, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<?>>>> map;

    private StaticRegistry(ImmutableMap<TypeIdentifier<?>, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<?>>>> map) {
      this.map = checkNotNull(map);
    }

    private <T> Optional<BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<?>>>> getGenFunction(TypeIdentifier<T> identifier) {
      return Optional.ofNullable(map.get(identifier))
          .map(Optional::of)
          .orElseGet(() -> map.entrySet().stream()
              .filter(entry -> Identifiers.areEquivalent(identifier, entry.getKey()))
              .map(Entry::getValue)
              .findFirst());
    }

    @Override
    public <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier) {
      Optional<Generator<?>> generator = getGenFunction(identifier).flatMap(f -> f.apply(root, identifier));
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

    public static <T, A> BiFunction<Registry, TypeIdentifier<A>, Optional<Generator<T>>> resolved(Function<Generator<A>, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType) -> registry.lookup(firstType).map(mapper);
    }

    public static <T, A, B> Function3<Registry, TypeIdentifier<A>, TypeIdentifier<B>, Optional<Generator<T>>> resolved(BiFunction<Generator<A>, Generator<B>, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType, secondType) ->
          registry.lookup(firstType)
              .flatMap(firstGen -> registry.lookup(secondType)
                  .map(secondGen -> mapper.apply(firstGen, secondGen)));
    }

    public static <T, A, B, C> Function4<Registry, TypeIdentifier<A>, TypeIdentifier<B>, TypeIdentifier<C>, Optional<Generator<T>>> resolved(Function3<Generator<A>, Generator<B>, Generator<C>, Generator<T>> mapper) {
      checkNotNull(mapper);
      return (registry, firstType, secondType, thirdType) ->
          registry.lookup(firstType)
              .flatMap(firstGen -> registry.lookup(secondType)
                  .flatMap(secondGen -> registry.lookup(thirdType)
                      .map(thirdGen -> mapper.apply( firstGen, secondGen, thirdGen))));
    }

    public static <T, A, B, C, D> Function5<Registry, TypeIdentifier<A>, TypeIdentifier<B>, TypeIdentifier<C>, TypeIdentifier<D>, Optional<Generator<T>>> resolved(Function4<Generator<A>, Generator<B>, Generator<C>, Generator<D>, Generator<T>> mapper) {
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
    private final ImmutableMap.Builder<TypeIdentifier<?>, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<?>>>> staticBuilder;
    private final ImmutableMap.Builder<Class<?>, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<?>>>> dynamicBuilder;

    private RegistryBuilder() {
      this.staticBuilder = ImmutableMap.builder();
      this.dynamicBuilder = ImmutableMap.builder();
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, Generator<T> generator) {
      staticBuilder.put(identifier, (r, i) -> Optional.of(generator));
      return this;
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, Function<Registry, Optional<Generator<T>>> generatorFactory) {
      staticBuilder.put(identifier, (r, i) -> (Optional) generatorFactory.apply(r));
      return this;
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<T>>> generatorFactory) {
      staticBuilder.put(identifier, (BiFunction) generatorFactory);
      return this;
    }

    public <T, A> RegistryBuilder putDyn(Class<? super T> identifier, BiFunction<Registry, TypeIdentifier<A>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 1 ? (Optional<Generator<T>>) ((BiFunction) generatorFactory).apply(registry, params.get(0)) : Optional.empty());
    }

    public <T, A, B> RegistryBuilder putDyn(Class<? super T> identifier, Function3<Registry, TypeIdentifier<A>, TypeIdentifier<B>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 2 ? (Optional<Generator<T>>) ((Function3) generatorFactory).apply(registry, params.get(0), params.get(1)) : Optional.empty());
    }

    public <T, A, B, C> RegistryBuilder putDyn(Class<? super T> identifier, Function4<Registry, TypeIdentifier<A>, TypeIdentifier<B>, TypeIdentifier<C>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 3 ? (Optional<Generator<T>>) ((Function4) generatorFactory).apply(registry, params.get(0), params.get(1), params.get(2)) : Optional.empty());
    }

    public <T, A, B, C, D> RegistryBuilder putDyn(Class<? super T> identifier, Function5<Registry, TypeIdentifier<A>, TypeIdentifier<B>, TypeIdentifier<C>, TypeIdentifier<D>, Optional<Generator<T>>> generatorFactory) {
      return putDynamic(identifier, (registry, params) ->
          params.size() == 4 ? (Optional<Generator<T>>) ((Function5) generatorFactory).apply(registry, params.get(0), params.get(1), params.get(2), params.get(3)) : Optional.empty());
    }

    public <T> RegistryBuilder putDynamic(Class<? super T> identifier, BiFunction<Registry, List<TypeIdentifier<?>>, Optional<Generator<T>>> generatorFactory) {
      dynamicBuilder.put(identifier, (BiFunction) generatorFactory);
      return this;
    }

    public Registry build() {
      ImmutableMap<TypeIdentifier<?>, BiFunction<Registry, TypeIdentifier<?>, Optional<Generator<?>>>> staticMap = staticBuilder.build();
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
