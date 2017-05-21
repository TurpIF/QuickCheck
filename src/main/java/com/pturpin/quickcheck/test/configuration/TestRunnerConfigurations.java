package com.pturpin.quickcheck.test.configuration;

import com.pturpin.quickcheck.base.Reflections;

import java.lang.reflect.Method;
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

  public static TestRunnerConfiguration reflectiveMethodConfiguration(Method method, TestRunnerConfiguration baseConfig) throws ReflectiveOperationException {
    checkNotNull(method);
    checkNotNull(baseConfig);

    TestConfiguration.NbRun nbRunAnnot = method.getAnnotation(TestConfiguration.NbRun.class);
    TestConfiguration.Skipped skippedAnnot = method.getAnnotation(TestConfiguration.Skipped.class);
    TestConfiguration.Random randomAnnot = method.getAnnotation(TestConfiguration.Random.class);
    TestConfiguration.Registry registryAnnot = method.getAnnotation(TestConfiguration.Registry.class);

    long nbRun = nbRunAnnot != null ? nbRunAnnot.value() : baseConfig.getNbRun();
    boolean acceptSkipped = skippedAnnot != null ? skippedAnnot.accept() : baseConfig.acceptSkipped();
    RandomFactory randomFactory = randomAnnot != null
        ? Reflections.newFactory(randomAnnot.value())
        : baseConfig.getRandomFactory();
    RegistryFactory registryFactory = registryAnnot != null
        ? Reflections.newFactory(registryAnnot.value())
        : baseConfig.getRegistryFactory();

    return new TestRunnerConfigurationImpl(nbRun, acceptSkipped, randomFactory, registryFactory);
  }

  public static Optional<TestRunnerConfiguration> reflectiveConfiguration(Class<?> klass) throws ReflectiveOperationException {
    TestConfiguration annotation = klass.getAnnotation(TestConfiguration.class);
    return annotation == null ? Optional.empty() : Optional.of(reflectiveConfiguration(annotation));
  }

  public static TestRunnerConfiguration reflectiveConfiguration(TestConfiguration config) throws ReflectiveOperationException {
    checkNotNull(config);
    long nbRun = config.nbRun() == TestConfiguration.NONE_NB_RUN ? DEFAULT_NB_RUN : config.nbRun();
    RandomFactory randomFactory = config.random() == TestConfiguration.NoneRandomFactory.class ? DEFAULT_RANDOM_FACTORY : Reflections.newFactory(config.random());
    RegistryFactory registryFactory = config.registry() == TestConfiguration.NoneRegistryFactory.class ? DEFAULT_REGISTRY_FACTORY : Reflections.newFactory(config.registry());

    return new TestRunnerConfigurationImpl(nbRun, config.acceptSkipped(), randomFactory, registryFactory);
  }

  public static TestRunnerConfiguration configuration(long nbRun, boolean acceptSkipped, RandomFactory random, RegistryFactory registry) {
    return new TestRunnerConfigurationImpl(nbRun, acceptSkipped, random, registry);
  }

  public static TestRunnerConfiguration defaultConfiguration() {
    return new TestRunnerConfigurationImpl(DEFAULT_NB_RUN, DEFAULT_ACCEPT_SKIPPED, DEFAULT_RANDOM_FACTORY, DEFAULT_REGISTRY_FACTORY);
  }

  private static final class TestRunnerConfigurationImpl implements TestRunnerConfiguration {

    private final long nbRun;
    private final boolean acceptSkipped;
    private final RandomFactory random;
    private final RegistryFactory registry;

    private TestRunnerConfigurationImpl(long nbRun, boolean acceptSkipped, RandomFactory random, RegistryFactory registry) {
      checkArgument(nbRun > 0);
      this.nbRun = nbRun;
      this.acceptSkipped = acceptSkipped;
      this.random = checkNotNull(random);
      this.registry = checkNotNull(registry);
    }

    @Override
    public long getNbRun() {
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

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestRunnerConfigurationImpl that = (TestRunnerConfigurationImpl) o;

      return nbRun == that.nbRun
          && acceptSkipped == that.acceptSkipped
          && random.equals(that.random)
          && registry.equals(that.registry);
    }

    @Override
    public int hashCode() {
      int result = (int) (nbRun ^ (nbRun >>> 32));
      result = 31 * result + (acceptSkipped ? 1 : 0);
      result = 31 * result + random.hashCode();
      result = 31 * result + registry.hashCode();
      return result;
    }
  }
}
