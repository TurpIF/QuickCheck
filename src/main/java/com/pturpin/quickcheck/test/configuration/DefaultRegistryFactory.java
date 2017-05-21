package com.pturpin.quickcheck.test.configuration;

import com.pturpin.quickcheck.identifier.ClassIdentifier;
import com.pturpin.quickcheck.registry.Registries;
import com.pturpin.quickcheck.registry.Registry;

import static com.pturpin.quickcheck.generator.Numbers.doubleGen;

/**
 * Created by turpif on 28/04/17.
 */
public final class DefaultRegistryFactory implements RegistryFactory {

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
