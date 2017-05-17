package com.pturpin.quickcheck.test.configuration;

/**
 * Created by pturpin on 17/05/2017.
 */
public interface TestRunnerConfiguration {

  int getNbRun();

  boolean acceptSkipped();

  RandomFactory getRandomFactory();

  RegistryFactory getRegistryFactory();

}
