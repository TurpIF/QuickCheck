package fr.pturpin.quickcheck.test.configuration;

import fr.pturpin.quickcheck.registry.Registry;

/**
 * Created by pturpin on 16/05/2017.
 */
@FunctionalInterface
public interface RegistryFactory {
  Registry create();
}
