package fr.pturpin.quickcheck.identifier;

import java.util.List;
import java.util.Optional;

public interface TypeIdentifier<T> {
  Class<T> getTypeClass();

  int getNbParametrizedType();

  Optional<List<TypeIdentifier<?>>> getParametrizedType();
}
