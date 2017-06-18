package fr.pturpin.quickcheck.identifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.common.primitives.Primitives;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Created by turpif on 09/06/17.
 */
public final class Identifiers {

  private Identifiers() {
    // nothing
  }

  /**
   * Returns a new named unbounded wildcard type identifier.
   * A wildcard is use to force equivalence with other type identifier.
   *
   * @see #areEquivalent(TypeIdentifier, TypeIdentifier)
   *
   * @param name non-empty name of the wildcard such as T
   * @return wildcard identifier
   * @throws NullPointerException if name is null
   * @throws IllegalArgumentException if name is empty
   */
  public static TypeIdentifier<Object> wildcardId(String name) {
    return new WildcardIdentifier(name);
  }

  /**
   * Returns a new wildcard type identifier.
   * A wildcard is use to force equivalence with other type identifier.
   *
   * @see #areEquivalent(TypeIdentifier, TypeIdentifier)
   *
   * @param type type of wildcard
   * @return wildcard identifier
   * @throws NullPointerException if type is null
   */
  public static TypeIdentifier<Object> wildcardId(TypeVariable<?> type) {
    return new WildcardIdentifier(type.getName());
  }

  /**
   * Returns a new type identifier from given class.
   * The identifier contains the given class and the type variables (as wildcard identifier) of the class.
   * If the given class is a class of primitive, it is replaced by its boxing version.
   *
   * @param klass Class to identify
   * @param <T> Type of class
   * @return class identifier
   * @throws NullPointerException if klass is null
   */
  public static <T> TypeIdentifier<T> classId(Class<? super T> klass) {
    return new ClassIdentifier<T>((Class) Primitives.wrap(klass));
  }

  /**
   * Returns a new parametrized type identifier from the base identifier and its parameters.
   *
   * @param ownerIdentifier base identifier
   * @param parameters parameter identifiers
   * @param <T> type of identifier
   * @return parametrized identifier
   * @throws NullPointerException if any identifiers is null
   */
  public static <T> TypeIdentifier<T> paramId(TypeIdentifier<? super T> ownerIdentifier, List<TypeIdentifier<?>> parameters) {
    return new ParametrizedIdentifier<T>((TypeIdentifier) ownerIdentifier, parameters);
  }

  /**
   * Returns a new parametrized type identifier from base class and its parameters
   * This function is a shortcut
   *
   * @see #classId(Class)
   *
   * @param klass base class of identifier
   * @param parameters parameter classes
   * @param <T> type of the identifier
   * @return parametrized identifier
   * @throws NullPointerException is any classes is null
   */
  public static <T> TypeIdentifier<T> paramId(Class<? super T> klass, Class<?>... parameters) {
    ImmutableList<TypeIdentifier<?>> typedParameters = Arrays.stream(parameters).map(k -> classId((Class<Object>) k)).collect(toImmutableList());
    return new ParametrizedIdentifier<>(classId(klass), typedParameters);
  }

  /**
   * Returns a new parametrized type identifier from the base class and its parameters.
   *
   * This function is a shortcut and is equivalent to {@code paramId(classId(klass), Arrays.asList(parameters))}
   *
   * @see #classId(Class)
   * @see #paramId(TypeIdentifier, List)
   *
   * @param klass base class of identifier
   * @param parameters parameter identifiers
   * @param <T> type of identifier
   * @return parametrized identifier
   * @throws NullPointerException if any input is null
   */
  public static <T> TypeIdentifier<T> paramId(Class<? super T> klass, TypeIdentifier<?>... parameters) {
    return new ParametrizedIdentifier<>(classId(klass), Arrays.asList(parameters));
  }

  /**
   * Try to fetch a new deep identifier from given {@link Type} instance.
   * This function is called deeply on all type tree to fetch a complete identifier representation.
   * <p>
   * Currently, only {@link Class}, {@link ParameterizedType}, {@link WildcardType} and {@link TypeVariable} are supported.
   * This method throw an {@link UnsupportedOperationException} if another kind of {@link Type} is retrieved.
   * <p>
   * For {@link WildcardType} (?, ? extends T, ? super T), only those with only one upper bound are currently supported.
   * A {@link IllegalStateException} is thrown for other wildcard type.
   *
   * @param type type to identify
   * @return deep type identifier
   */
  public static TypeIdentifier<Object> typeId(Type type) {
    if (type instanceof Class<?>) {
      return classId((Class) type);
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      TypeIdentifier<Object> rawIdentifier = typeId(parameterizedType.getRawType());
      List<TypeIdentifier<?>> paramIdentifiers = Arrays.stream(parameterizedType.getActualTypeArguments())
          .map(Identifiers::typeId)
          .collect(toImmutableList());
      return paramId(rawIdentifier, paramIdentifiers);
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      Type[] upperBounds = wildcardType.getUpperBounds();
      Preconditions.checkState(
          upperBounds.length == 1, "Wildcard type are handled only with single upper bound: %s", type);
      return typeId(upperBounds[0]);
    } else if (type instanceof TypeVariable) {
      return wildcardId(((TypeVariable) type));
    }
    throw new UnsupportedOperationException("Not supported type: " + type);
  }

  /**
   * Indicates if given identifiers are equivalent.
   *
   * Two identifiers are equivalent if they are equals when using wildcard rule :
   * a wildcard type identifier is equivalent to any other identifier.
   *
   * @param left left identifier
   * @param right right identifier
   * @return true if both are equivalent
   */
  public static boolean areEquivalent(TypeIdentifier<?> left, TypeIdentifier<?> right) {
    checkNotNull(left);
    checkNotNull(right);

    if (left.equals(right) || left instanceof WildcardIdentifier || right instanceof WildcardIdentifier) {
      return true;
    }
    return left.getTypeClass().equals(right.getTypeClass())
        && left.getParametrizedType().isPresent()
        && right.getParametrizedType().isPresent()
        && Streams.zip(left.getParametrizedType().get().stream(), right.getParametrizedType().get().stream(), Identifiers::areEquivalent).allMatch(b -> b);
  }

  /**
   * In the given identifier tree, replace the element in keys of given map by their respective values.
   * <br/>
   * For instance, given the map @{code [String -> Double]} and the identifier of @{code Map<String, Integer>},
   * the result should be {@code Map<Double, Integer>}
   * <br/>
   * The given instance is not mutated. A new independent instance is returned
   *
   * @param replaceMap replacing map
   * @param id identifier to replace in
   * @return replaced identifier
   */
  public static TypeIdentifier<?> replace(Map<? extends TypeIdentifier<?>, ? extends TypeIdentifier<?>> replaceMap, TypeIdentifier<?> id) {
    return replace(k -> Optional.ofNullable(replaceMap.get(k)), id);
  }

  /**
   * In the given identifier tree, replace the element using the given function.
   * If function return an empty, the tree element remained the same, else it's replaced by the returned value.
   *
   * <br/>
   * For instance, given the function mapping @{code [String -> Double]} and the identifier of @{code Map<String, Integer>},
   * the result should be {@code Map<Double, Integer>}
   * <br/>
   * The given instance is not mutated. A new independent instance is returned
   *
   * @param replaceF replacing function
   * @param id identifier to replace in
   * @return replaced identifier
   */
  public static TypeIdentifier<?> replace(Function<TypeIdentifier<?>, Optional<TypeIdentifier<?>>> replaceF, TypeIdentifier<?> id) {
    return replaceF.apply(id).orElseGet(() -> {
      Optional<TypeIdentifier> replacedId = id.getParametrizedType()
          .map(subIds -> subIds.stream()
              .map(subId -> replace(replaceF, subId))
              .collect(toImmutableList()))
          .map(subIds -> paramId(classId((Class) id.getTypeClass()), (List) subIds));
      return replacedId.orElse(id);
    });
  }

  /**
   * Returns a map of fetched wildcard resolution by comparing given wildcarded type and given resolved type.
   * Both type identifier should have the same structure except that wildcard in wildcarded identifier may
   * represent any type in resolved identifier.
   * <p>
   * For example, given a wildcarded type {@code Map<K, List<V>} and the resolved type
   * {@code Map<Supplier<T>, List<String>>} will produce the resolution map : {@code [K -> Supplier<T>, V -> String]}
   * <p>
   * If both type identifier don't have se same structure even with wildcard, an {@link IllegalStateException} is thrown.
   *
   * @param wildcardedId type identifier to find the resolution of wildcard
   * @param resolvedId resolved identifier used a resolution model
   * @return resolution map
   * @throws IllegalStateException if given identifiers don't have the same structure
   * @throws NullPointerException if any identifier is null
   */
  public static ImmutableMap<WildcardIdentifier, TypeIdentifier<?>> fetchWilcardResolution(TypeIdentifier<?> wildcardedId, TypeIdentifier<?> resolvedId) {
    checkNotNull(wildcardedId);
    checkNotNull(resolvedId);
    ImmutableMap.Builder<WildcardIdentifier, TypeIdentifier<?>> builder = ImmutableMap.builder();
    buildWildcardResolution(wildcardedId, resolvedId, builder);
    return builder.build();
  }

  private static void buildWildcardResolution(TypeIdentifier<?> wildcardedId, TypeIdentifier<?> resolvedId, ImmutableMap.Builder<WildcardIdentifier, TypeIdentifier<?>> builder) {
    if (wildcardedId instanceof WildcardIdentifier) {
      builder.put((WildcardIdentifier) wildcardedId, resolvedId);
    } else if (wildcardedId.getParametrizedType().isPresent()) {
      List<TypeIdentifier<?>> rawSubIds = wildcardedId.getParametrizedType().get();
      List<TypeIdentifier<?>> runtimeSubIds = resolvedId.getParametrizedType().get();
      for (int i = 0; i < rawSubIds.size(); i++) {
        buildWildcardResolution(rawSubIds.get(i), runtimeSubIds.get(i), builder);
      }
    }
  }
}
