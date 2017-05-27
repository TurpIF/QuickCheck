package com.pturpin.quickcheck.test.configuration;

import com.pturpin.quickcheck.registry.Registry;

/**
 * Created by pturpin on 16/05/2017.
 */
@FunctionalInterface
public interface RegistryFactory {
  Registry create();
}
