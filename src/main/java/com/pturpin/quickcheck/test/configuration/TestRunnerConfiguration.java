package com.pturpin.quickcheck.test.configuration;

/**
 * Created by pturpin on 17/05/2017.
 */
public interface TestRunnerConfiguration {

  long getNbRun();

  double acceptSkipped();

  RandomFactory getRandomFactory();

  RegistryFactory getRegistryFactory();

  @FunctionalInterface
  interface TestConfigurationMapper {
    TestRunnerConfiguration map(TestRunnerConfiguration baseConfig);
  }

}
