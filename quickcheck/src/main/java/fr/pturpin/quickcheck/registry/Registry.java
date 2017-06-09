package fr.pturpin.quickcheck.registry;

import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;

import java.util.Optional;

/**
 * Registry interface to search generator for a given identifier.
 *
 * Implementations should not implement the {@link #lookup(TypeIdentifier)} method.
 */
public interface Registry {

  // should not be implemented
  default <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier) {
    return recursiveLookup(this, identifier);
  }

  <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier);
}
