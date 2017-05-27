package fr.pturpin.quickcheck.registry;

import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;

import java.util.Optional;

@FunctionalInterface
public interface Registry {
  <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier);
}
