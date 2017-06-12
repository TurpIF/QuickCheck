package fr.pturpin.quickcheck.identifier;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class ClassIdentifier<T> implements TypeIdentifier<T> {
  private final Class<T> klass;
  private final int nbParametrizedType;

  ClassIdentifier(Class<T> klass, int nbParametrizedType) {
    checkArgument(nbParametrizedType >= 0);
    this.klass = checkNotNull(klass);
    this.nbParametrizedType = nbParametrizedType;
  }

  @Override
  public Class<T> getTypeClass() {
    return klass;
  }

  @Override
  public int getNbParametrizedType() {
    return nbParametrizedType;
  }

  @Override
  public Optional<List<TypeIdentifier<?>>> getParametrizedType() {
    return nbParametrizedType == 0 ? Optional.of(ImmutableList.of()) : Optional.empty();
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
    return klass.equals(other.klass) && nbParametrizedType == other.nbParametrizedType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(klass, nbParametrizedType);
  }
}
