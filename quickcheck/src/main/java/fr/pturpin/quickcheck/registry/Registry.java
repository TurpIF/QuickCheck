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

  /**
   * Try to fetch a generator with the given identifier in this registry.
   *
   * <b>This default method should not be overridden by implementations.</b>
   *
   * @param identifier identifier of generator to fetch
   * @param <T> type of elements yield by the generator
   * @return either empty or the found generator
   */
  default <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier) {
    return recursiveLookup(this, identifier);
  }

  /**
   * Try to fetch a generator with the given identifier in this registry.
   * If the found generator need other generator as dependencies, it may find it recursively in given root registry.
   *
   * @param root root registry to use for recursive dependency lookup up
   * @param identifier identifier of generator to fetch
   * @param <T> type of elements yield by the generator
   * @return either empty of the found generator
   */
  <T> Optional<Generator<T>> recursiveLookup(Registry root, TypeIdentifier<T> identifier);
}
