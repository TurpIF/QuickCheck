package fr.pturpin.quickcheck.identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

final class ClassIdentifier<T> implements TypeIdentifier<T> {
  private final Class<T> klass;

  ClassIdentifier(Class<T> klass) {
    this.klass = checkNotNull(klass);
  }

  @Override
  public Class<T> getTypeClass() {
    return klass;
  }

  @Override
  public int getNbParametrizedType() {
    return klass.getTypeParameters().length;
  }

  @Override
  public Optional<List<TypeIdentifier<?>>> getParametrizedType() {
    return Optional.of(Arrays.stream(klass.getTypeParameters())
        .map(Identifiers::wildcardId)
        .collect(toImmutableList()));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != ClassIdentifier.class) {
      return false;
    }
    ClassIdentifier other = (ClassIdentifier) obj;
    return klass.equals(other.klass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(klass);
  }

  @Override
  public String toString() {
    return TypeIdentifier.idToString(this);
  }
}
