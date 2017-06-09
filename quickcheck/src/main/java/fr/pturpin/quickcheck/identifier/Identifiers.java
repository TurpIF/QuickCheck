package fr.pturpin.quickcheck.identifier;

import com.google.common.base.Preconditions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Created by turpif on 09/06/17.
 */
public final class Identifiers {

  private Identifiers() {
    // nothing
  }

  public static <T> TypeIdentifier<T> classId(Class<T> klass) {
    return new ClassIdentifier<>(klass);
  }

  public static <T> ParametrizedIdentifier<T> paramId(TypeIdentifier<T> ownerIdentifier, List<TypeIdentifier<?>> parameters) {
    return new ParametrizedIdentifier<>(ownerIdentifier, parameters);
  }

  public static <T> ParametrizedIdentifier<T> paramId(Class<T> klass, Class<?>... parameters) {
    return new ParametrizedIdentifier<>(new ClassIdentifier<>(klass), Arrays.stream(parameters).map(p -> new ClassIdentifier<>(p)).collect(toImmutableList()));
  }

  public static <T> ParametrizedIdentifier<T> paramId(Class<T> klass, TypeIdentifier<?>... parameters) {
    return new ParametrizedIdentifier<>(new ClassIdentifier<>(klass), Arrays.asList(parameters));
  }

  public static TypeIdentifier<Object> reflectiveId(Type type) {
    if (type instanceof Class<?>) {
      return classId((Class) type);
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      TypeIdentifier<Object> rawIdentifier = reflectiveId(parameterizedType.getRawType());
      List<TypeIdentifier<?>> paramIdentifiers = Arrays.stream(parameterizedType.getActualTypeArguments())
          .map(Identifiers::reflectiveId)
          .collect(toImmutableList());
      return paramId(rawIdentifier, paramIdentifiers);
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      Type[] upperBounds = wildcardType.getUpperBounds();
      Preconditions.checkState(
          upperBounds.length == 1, "Wildcard type are handled only with single upper bound: %s", type);
      return reflectiveId(upperBounds[0]);
    }
    throw new UnsupportedOperationException("Not supported type: " + type);
  }
}
