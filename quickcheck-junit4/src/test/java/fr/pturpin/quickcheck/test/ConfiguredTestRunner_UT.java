package fr.pturpin.quickcheck.test;

import com.google.common.collect.ImmutableSet;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.junit4.QuickCheck;
import fr.pturpin.quickcheck.test.configuration.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;
import static fr.pturpin.quickcheck.test.TestResult.when;

/**
 * Created by turpif on 21/05/17.
 */
public class ConfiguredTestRunner_UT {

  @Test
  public void testsWithoutConfigShouldUseDefaultConfiguration() {
    checkClassConfiguration(WithoutAnnotationTests.class,
        TestRunnerConfigurations.defaultConfiguration(),
        () -> WithoutAnnotationTests.state = new ConfiguredTestRunner.State(),
        () -> WithoutAnnotationTests.state);
  }

  @Test
  public void testClassConfiguration() {
    Set<RegistryFactory> registryFactories = ImmutableSet.of(
        new ConfiguredTestRunner.EmptyRegistryFactory(),
        new ConfiguredTestRunner.SpecialDoubleRegistryFactory(),
        new DefaultRegistryFactory());

    Set<RandomFactory> randomFactories = ImmutableSet.of(
        new ConfiguredTestRunner.RandomSeed1Factory(),
        new ConfiguredTestRunner.RandomSeed2Factory(),
        new DefaultRandomFactory());

    Set<Integer> nbRuns = ImmutableSet.of(1, 10, 50);
    Set<Double> acceptSkippeds = ImmutableSet.of(0.0, 0.25, 0.5, 0.75, 1.0);

    nbRuns.stream()
      .flatMap(nbRun -> acceptSkippeds.stream()
        .flatMap(acceptSkipped -> registryFactories.stream()
            .flatMap(registryFactory -> randomFactories.stream()
                .map(randomFactory ->
                    TestRunnerConfigurations.configuration(nbRun, acceptSkipped, randomFactory, registryFactory)))))
    .forEach(ConfiguredTestRunner.unchecked(ConfiguredTestRunner_UT::checkClassConfiguration));
  }

  @Test
  public void testUpdatingAnnotation() throws Exception {
    TestRunnerConfiguration settedConfig = TestRunnerConfigurations.defaultConfiguration();
    updateAnnotationValue(settedConfig);
    TestRunnerConfiguration config = TestRunnerConfigurations.reflectiveConfiguration(WithAnnotationTests.class).get();

    Assert.assertEquals(settedConfig.getNbRun(), config.getNbRun());
    Assert.assertEquals(settedConfig.acceptSkipped(), config.acceptSkipped(), 0);

    settedConfig = TestRunnerConfigurations.configuration(settedConfig.getNbRun() + 1,
        1.0 - settedConfig.acceptSkipped(), settedConfig.getRandomFactory(), settedConfig.getRegistryFactory());

    Assert.assertNotEquals(settedConfig.getNbRun(), config.getNbRun());
    Assert.assertNotEquals(settedConfig.acceptSkipped(), config.acceptSkipped(), 0);

    updateAnnotationValue(settedConfig);
    config = TestRunnerConfigurations.reflectiveConfiguration(WithAnnotationTests.class).get();

    Assert.assertEquals(settedConfig.getNbRun(), config.getNbRun());
    Assert.assertEquals(settedConfig.acceptSkipped(), config.acceptSkipped(), 0);
  }

  @RunWith(QuickCheck.class)
  public static final class WithoutAnnotationTests {
    static ConfiguredTestRunner.State state = new ConfiguredTestRunner.State();
    static int skipCounter = 0;
    @Test @TestConfiguration.Skipped(0.0) public void setCounter() {
      state.counter++;
    }
    @Test public void setFirstDouble(double v) {
      if (!state.firstDoubleInitialized) {
        state.firstDoubleInitialized = true;
        state.firstDouble = v;
      }
    }
    @Test @TestConfiguration.NbRun(1000) public TestResult skip() {
      return when(++skipCounter % 2 == 0, () -> {});
    }
  }

  @RunWith(QuickCheck.class)
  @TestConfiguration
  public static final class WithAnnotationTests {
    static ConfiguredTestRunner.State state = new ConfiguredTestRunner.State();
    static int skipCounter = 0;
    @Test public void setCounter() {
      state.counter++;
    }
    @Test public void setFirstDouble(double v) {
      if (!state.firstDoubleInitialized) {
        state.firstDoubleInitialized = true;
        state.firstDouble = v;
      }
    }
    @Test @TestConfiguration.NbRun(1000) public TestResult skip() {
      return when(++skipCounter % 2 == 0, () -> {});
    }
  }

  private static void checkClassConfiguration(TestRunnerConfiguration config) throws Exception {
    updateAnnotationValue(config);
    checkClassConfiguration(WithAnnotationTests.class, config,
        () -> WithAnnotationTests.state = new ConfiguredTestRunner.State(),
        () -> WithAnnotationTests.state);
  }

  private static void checkClassConfiguration(Class<?> klass,
      TestRunnerConfiguration config,
      Runnable stateInit,
      Supplier<ConfiguredTestRunner.State> stateGetter) {
    Optional<Generator<Double>> optDoubleGenerator = config.getRegistryFactory()
        .create()
        .lookup(classId(double.class));
    Random random = config.getRandomFactory().create();

    List<String> expectedFailedMethodNames = new ArrayList<>();
    if (!optDoubleGenerator.isPresent()) {
      expectedFailedMethodNames.add("setFirstDouble");
    }
    if (config.acceptSkipped() <= 0.5) {
      expectedFailedMethodNames.add("skip");
    }

    stateInit.run();
    JUnitCore junit = new JUnitCore();
    Result result = junit.run(klass);
    ConfiguredTestRunner.State state = stateGetter.get();

    Assert.assertEquals(config.getNbRun(), state.counter);

    if (optDoubleGenerator.isPresent()) {
      Generator<Double> doubleGenerator = optDoubleGenerator.get();
      Assert.assertTrue(state.firstDoubleInitialized);
      Assert.assertEquals(doubleGenerator.get(random), state.firstDouble, 0);
    } else {
      Assert.assertFalse(state.firstDoubleInitialized);
    }

    List<String> failedMethodNames = result.getFailures().stream()
        .map(fail -> fail.getDescription().getMethodName())
        .sorted()
        .collect(Collectors.toList());
    Assert.assertEquals(expectedFailedMethodNames, failedMethodNames);

    if (expectedFailedMethodNames.isEmpty()) {
      Assert.assertTrue(result.wasSuccessful());
    }
  }

  private static void updateAnnotationValue(TestRunnerConfiguration config) throws Exception {
    Class<?> klass = WithAnnotationTests.class;
    Method method = Class.class.getDeclaredMethod("getDeclaredAnnotationMap");
    method.setAccessible(true);
    Map<Class<? extends Annotation>, Annotation> annotationsMap = (Map) method.invoke(klass);
    TestConfiguration oldAnnot = (TestConfiguration) annotationsMap.get(TestConfiguration.class);

    TestConfiguration newAnnot = new TestConfiguration() {
      @Override public Class<? extends Annotation> annotationType() {
        return oldAnnot.annotationType();
      }
      @Override public long nbRun() {
        return config.getNbRun();
      }
      @Override public double acceptSkipped() {
        return config.acceptSkipped();
      }
      @Override public Class<? extends RandomFactory> random() {
        return config.getRandomFactory().getClass();
      }
      @Override public Class<? extends RegistryFactory> registry() {
        return config.getRegistryFactory().getClass();
      }
      @Override public int hashCode() {
        return oldAnnot.hashCode();
      }
      @Override public boolean equals(Object o) {
        return oldAnnot.equals(o);
      }
      @Override public String toString() {
        return oldAnnot.toString();
      }
    };
    annotationsMap.put(TestConfiguration.class, newAnnot);
  }
}
