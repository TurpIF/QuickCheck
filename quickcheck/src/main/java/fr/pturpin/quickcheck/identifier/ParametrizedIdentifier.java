package fr.pturpin.quickcheck.identifier;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Created by pturpin on 04/06/2017.
 */
public class ParametrizedIdentifier<T> implements TypeIdentifier<T> {

  private final TypeIdentifier<T> ownerIdentifier;
  private final List<TypeIdentifier<?>> parameters;

  private ParametrizedIdentifier(TypeIdentifier<T> ownerIdentifier, List<TypeIdentifier<?>> parameters) {
    this.ownerIdentifier = checkNotNull(ownerIdentifier);
    this.parameters = ImmutableList.copyOf(checkNotNull(parameters));
  }

  public static <T> ParametrizedIdentifier<T> create(TypeIdentifier<T> ownerIdentifier, List<TypeIdentifier<?>> parameters) {
    return new ParametrizedIdentifier<>(ownerIdentifier, parameters);
  }

  public static <T> ParametrizedIdentifier<T> create(Class<T> klass, Class<?>... parameters) {
    return new ParametrizedIdentifier<>(new ClassIdentifier<>(klass), Arrays.stream(parameters).map(p -> new ClassIdentifier<>(p)).collect(toImmutableList()));
  }

  @Override
  public Class<T> getTypeClass() {
    return ownerIdentifier.getTypeClass();
  }

  public TypeIdentifier<T> getOwnerIdentifier() {
    return ownerIdentifier;
  }

  public List<TypeIdentifier<?>> getParameters() {
    return parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParametrizedIdentifier)) {
      return false;
    }
    ParametrizedIdentifier<?> that = (ParametrizedIdentifier<?>) o;
    return Objects.equals(ownerIdentifier, that.ownerIdentifier) &&
        Objects.equals(parameters, that.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerIdentifier, parameters);
  }
}
