package fr.pturpin.quickcheck.identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by pturpin on 17/06/2017.
 */
public final class WildcardIdentifier implements TypeIdentifier<Object> {

  private final String name;

  WildcardIdentifier(String name) {
    checkArgument(!name.isEmpty());
    this.name = name;
  }

  @Override
  public Class<Object> getTypeClass() {
    return Object.class;
  }

  @Override
  public int getNbParametrizedType() {
    return 0;
  }

  @Override
  public Optional<List<TypeIdentifier<?>>> getParametrizedType() {
    return Optional.empty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WildcardIdentifier)) {
      return false;
    }
    WildcardIdentifier that = (WildcardIdentifier) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name;
  }
}
