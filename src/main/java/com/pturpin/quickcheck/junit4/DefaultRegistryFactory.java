package com.pturpin.quickcheck.junit4;

import com.pturpin.quickcheck.identifier.ClassIdentifier;
import com.pturpin.quickcheck.registry.Registries;
import com.pturpin.quickcheck.registry.Registry;

import static com.pturpin.quickcheck.generator.Numbers.doubleGen;

/**
 * Created by turpif on 28/04/17.
 */
final class DefaultRegistryFactory implements RandomRunner.RegistryFactory {

  public DefaultRegistryFactory() {
    // nothing
  }

  @Override
  public Registry create() {
    return Registries.builder()
        .put(new ClassIdentifier<>(double.class), doubleGen())
        .put(new ClassIdentifier<>(Double.class), doubleGen())
        .build();
  }
}