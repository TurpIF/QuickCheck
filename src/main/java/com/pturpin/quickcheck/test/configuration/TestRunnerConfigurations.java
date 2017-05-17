package com.pturpin.quickcheck.test.configuration;

import com.pturpin.quickcheck.base.Reflections;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by pturpin on 17/05/2017.
 */
public class TestRunnerConfigurations {

  private static final int DEFAULT_NB_RUN = 100;
  static final boolean DEFAULT_ACCEPT_SKIPPED = false;
  private static final RandomFactory DEFAULT_RANDOM_FACTORY = new DefaultRandomFactory();
  private static final RegistryFactory DEFAULT_REGISTRY_FACTORY = new DefaultRegistryFactory();

  public static Optional<TestRunnerConfiguration> reflectiveConfiguration(Class<?> klass) throws ReflectiveOperationException {
    TestConfiguration annotation = klass.getAnnotation(TestConfiguration.class);
    return annotation == null ? Optional.empty() : Optional.of(reflectiveConfiguration(annotation));
  }

  private static TestRunnerConfiguration reflectiveConfiguration(TestConfiguration config) throws ReflectiveOperationException {
    checkNotNull(config);
    int nbRun = config.nbRun() == TestConfiguration.NONE_NB_RUN ? DEFAULT_NB_RUN : config.nbRun();
    RandomFactory randomFactory = config.random() == TestConfiguration.NoneRandomFactory.class ? DEFAULT_RANDOM_FACTORY : Reflections.newFactory(config.random());
    RegistryFactory registryFactory = config.registry() == TestConfiguration.NoneRegistryFactory.class ? DEFAULT_REGISTRY_FACTORY : Reflections.newFactory(config.registry());

    return new TestRunnerConfigurationImpl(nbRun, config.acceptSkipped(), randomFactory, registryFactory);
  }

  public static TestRunnerConfiguration defaultConfiguration() {
    return new TestRunnerConfigurationImpl(DEFAULT_NB_RUN, DEFAULT_ACCEPT_SKIPPED, DEFAULT_RANDOM_FACTORY, DEFAULT_REGISTRY_FACTORY);
  }

  private static final class TestRunnerConfigurationImpl implements TestRunnerConfiguration {

    private final int nbRun;
    private final boolean acceptSkipped;
    private final RandomFactory random;
    private final RegistryFactory registry;

    private TestRunnerConfigurationImpl(int nbRun, boolean acceptSkipped, RandomFactory random, RegistryFactory registry) {
      checkArgument(nbRun > 0);
      this.nbRun = nbRun;
      this.acceptSkipped = acceptSkipped;
      this.random = checkNotNull(random);
      this.registry = checkNotNull(registry);
    }

    @Override
    public int getNbRun() {
      return nbRun;
    }

    @Override
    public boolean acceptSkipped() {
      return acceptSkipped;
    }

    @Override
    public RandomFactory getRandomFactory() {
      return random;
    }

    @Override
    public RegistryFactory getRegistryFactory() {
      return registry;
    }
  }
}
