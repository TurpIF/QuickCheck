package fr.pturpin.quickcheck.identifier;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by pturpin on 04/06/2017.
 */
class ParametrizedIdentifier<T> implements TypeIdentifier<T> {

  private final TypeIdentifier<T> ownerIdentifier;
  private final List<TypeIdentifier<?>> parameters;

  ParametrizedIdentifier(TypeIdentifier<T> ownerIdentifier, List<TypeIdentifier<?>> parameters) {
    this.ownerIdentifier = checkNotNull(ownerIdentifier);
    this.parameters = ImmutableList.copyOf(checkNotNull(parameters));
  }

  @Override
  public Class<T> getTypeClass() {
    return ownerIdentifier.getTypeClass();
  }

  @Override
  public int getNbParametrizedType() {
    return parameters.size();
  }

  @Override
  public Optional<List<TypeIdentifier<?>>> getParametrizedType() {
    return Optional.of(parameters);
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

  @Override
  public String toString() {
    return TypeIdentifier.idToString(this);
  }
}
