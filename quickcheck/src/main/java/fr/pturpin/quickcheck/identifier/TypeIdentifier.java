package fr.pturpin.quickcheck.identifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface TypeIdentifier<T> {
  Class<T> getTypeClass();

  int getNbParametrizedType();

  Optional<List<TypeIdentifier<?>>> getParametrizedType();

  static String idToString(TypeIdentifier<?> id) {
    return id.getTypeClass().getSimpleName() + id.getParametrizedType()
        .map(parameters -> parameters.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", ", "<", ">")))
        .orElseGet(() -> id.getNbParametrizedType() == 0 ? "" : IntStream.range(0, id.getNbParametrizedType())
            .mapToObj(i -> "?")
            .collect(Collectors.joining(", ", "<", ">")));
  }
}
