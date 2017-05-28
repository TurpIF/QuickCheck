package fr.pturpin.quickcheck.test.configuration;

import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Random;

import static fr.pturpin.quickcheck.test.configuration.TestRunnerConfigurations.reflectiveConfiguration;
import static fr.pturpin.quickcheck.test.configuration.TestRunnerConfigurations.reflectiveMethodConfiguration;

/**
 * Created by turpif on 21/05/17.
 */
public class TestRunnerConfiguration_UT {

  private static final long NB_RUN = 1337;
  private static final double ACCEPT_SKIPPED = 0.0;

  private Method withoutConfigMethod;
  private Method withRunConfigMethod;
  private Method withAcceptSkippedConfigMethod;
  private Method withRandomConfigMethod;
  private Method withRegistryConfigMethod;

  @Before
  public void before() throws NoSuchMethodException {
    withoutConfigMethod = TestRunnerConfiguration_UT.class.getDeclaredMethod("withoutConfigMethod");
    withRunConfigMethod = TestRunnerConfiguration_UT.class.getDeclaredMethod("withRunConfigMethod");
    withAcceptSkippedConfigMethod = TestRunnerConfiguration_UT.class.getDeclaredMethod("withAcceptSkippedConfigMethod");
    withRandomConfigMethod = TestRunnerConfiguration_UT.class.getDeclaredMethod("withRandomConfigMethod");
    withRegistryConfigMethod = TestRunnerConfiguration_UT.class.getDeclaredMethod("withRegistryConfigMethod");
  }

  @Test
  public void testReflectiveConfiguration() throws Exception {
    Optional<TestRunnerConfiguration> withoutAnnot = reflectiveConfiguration(WithoutConfiguration.class);
    Optional<TestRunnerConfiguration> withAnnot = reflectiveConfiguration(WithConfiguration.class);

    Assert.assertFalse(withoutAnnot.isPresent());
    Assert.assertTrue(withAnnot.isPresent());

    TestRunnerConfiguration config = withAnnot.get();
    Assert.assertEquals(NB_RUN, config.getNbRun());
    Assert.assertEquals(ACCEPT_SKIPPED, config.acceptSkipped(), 0);
    Assert.assertEquals(MyRandomFactory.class, config.getRandomFactory().getClass());
    Assert.assertEquals(MyRegistryFactory.class, config.getRegistryFactory().getClass());
  }

  @Test
  public void methodConfigWithoutConfigShouldStayUnchanged() throws Exception {
    TestRunnerConfiguration base = reflectiveConfiguration(WithConfiguration.class).get();
    TestRunnerConfiguration methodConfig = reflectiveMethodConfiguration(withoutConfigMethod, base);
    Assert.assertEquals(base, methodConfig);
  }

  @Test
  public void testMethodConfigWithRunConfig() throws Exception {
    TestRunnerConfiguration base = reflectiveConfiguration(WithConfiguration.class).get();
    TestRunnerConfiguration methodConfig = reflectiveMethodConfiguration(withRunConfigMethod, base);
    Assert.assertEquals(1024, methodConfig.getNbRun());
    Assert.assertEquals(base.acceptSkipped(), methodConfig.acceptSkipped(), 0);
    Assert.assertEquals(base.getRandomFactory(), methodConfig.getRandomFactory());
    Assert.assertEquals(base.getRegistryFactory(), methodConfig.getRegistryFactory());
  }

  @Test
  public void testMethodConfigWithSkippedConfig() throws Exception {
    TestRunnerConfiguration base = reflectiveConfiguration(WithConfiguration.class).get();
    TestRunnerConfiguration methodConfig = reflectiveMethodConfiguration(withAcceptSkippedConfigMethod, base);
    Assert.assertEquals(base.getNbRun(), methodConfig.getNbRun());
    Assert.assertEquals(0.0, methodConfig.acceptSkipped(), 0);
    Assert.assertEquals(base.getRandomFactory(), methodConfig.getRandomFactory());
    Assert.assertEquals(base.getRegistryFactory(), methodConfig.getRegistryFactory());
  }

  @Test
  public void testMethodConfigWithRandomConfig() throws Exception {
    TestRunnerConfiguration base = reflectiveConfiguration(WithConfiguration.class).get();
    TestRunnerConfiguration methodConfig = reflectiveMethodConfiguration(withRandomConfigMethod, base);
    Assert.assertEquals(base.getNbRun(), methodConfig.getNbRun());
    Assert.assertEquals(base.acceptSkipped(), methodConfig.acceptSkipped(), 0);
    Assert.assertEquals(DefaultRandomFactory.class, methodConfig.getRandomFactory().getClass());
    Assert.assertEquals(base.getRegistryFactory(), methodConfig.getRegistryFactory());
  }

  @Test
  public void testMethodConfigWithRegistryConfig() throws Exception {
    TestRunnerConfiguration base = reflectiveConfiguration(WithConfiguration.class).get();
    TestRunnerConfiguration methodConfig = reflectiveMethodConfiguration(withRegistryConfigMethod, base);
    Assert.assertEquals(base.getNbRun(), methodConfig.getNbRun());
    Assert.assertEquals(base.acceptSkipped(), methodConfig.acceptSkipped(), 0);
    Assert.assertEquals(base.getRandomFactory(), methodConfig.getRandomFactory());
    Assert.assertEquals(DefaultRegistryFactory.class, methodConfig.getRegistryFactory().getClass());
  }

  private static void withoutConfigMethod() {}

  @TestConfiguration.NbRun(1024)
  private static void withRunConfigMethod() {}

  @TestConfiguration.Skipped(0.0)
  private static void withAcceptSkippedConfigMethod() {}

  @TestConfiguration.Random(DefaultRandomFactory.class)
  private static void withRandomConfigMethod() {}

  @TestConfiguration.Registry(DefaultRegistryFactory.class)
  private static void withRegistryConfigMethod() {}

  private static final class WithoutConfiguration {}

  @TestConfiguration(nbRun=NB_RUN, acceptSkipped=0.0, random=MyRandomFactory.class, registry=MyRegistryFactory.class)
  private static final class WithConfiguration {}

  private static final class MyRandomFactory implements RandomFactory {
    public MyRandomFactory() {
    }

    @Override
    public Random create() {
      return new Random(10);
    }
  }

  private static final class MyRegistryFactory implements RegistryFactory {
    public MyRegistryFactory() {
    }

    @Override
    public Registry create() {
      return Registries.empty();
    }
  }
}
