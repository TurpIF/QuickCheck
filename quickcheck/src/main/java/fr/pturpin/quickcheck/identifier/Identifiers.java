package fr.pturpin.quickcheck.identifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;

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

  public static <T> TypeIdentifier<T> classId(Class<? super T> klass) {
    return new ClassIdentifier<T>((Class) Primitives.wrap(klass), klass.getTypeParameters().length);
  }

  public static <T> TypeIdentifier<T> paramId(TypeIdentifier<? super T> ownerIdentifier, List<TypeIdentifier<?>> parameters) {
    return new ParametrizedIdentifier<T>((TypeIdentifier) ownerIdentifier, parameters);
  }

  public static <T> TypeIdentifier<T> paramId(Class<? super T> klass, Class<?>... parameters) {
    ImmutableList<TypeIdentifier<?>> typedParameters = Arrays.stream(parameters).map(k -> classId((Class<Object>) k)).collect(toImmutableList());
    return new ParametrizedIdentifier<>(classId(klass), typedParameters);
  }

  public static <T> TypeIdentifier<T> paramId(Class<? super T> klass, TypeIdentifier<?>... parameters) {
    return new ParametrizedIdentifier<>(classId(klass), Arrays.asList(parameters));
  }

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
    }
    throw new UnsupportedOperationException("Not supported type: " + type);
  }
}
