package com.pturpin.quickcheck.registry;

import com.pturpin.quickcheck.generator.Generator;
import com.pturpin.quickcheck.identifier.TypeIdentifier;

import java.util.Optional;

@FunctionalInterface
public interface Registry {
  <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier);
}
