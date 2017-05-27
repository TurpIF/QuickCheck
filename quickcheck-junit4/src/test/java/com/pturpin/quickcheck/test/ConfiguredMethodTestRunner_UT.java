package com.pturpin.quickcheck.test;

import com.google.common.collect.ImmutableSet;
import com.pturpin.quickcheck.generator.Generator;
import com.pturpin.quickcheck.identifier.ClassIdentifier;
import com.pturpin.quickcheck.junit4.RandomRunner;
import com.pturpin.quickcheck.test.ConfiguredTestRunner.*;
import com.pturpin.quickcheck.test.configuration.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static com.pturpin.quickcheck.test.ConfiguredTestRunner.*;
import static com.pturpin.quickcheck.test.TestResult.when;

/**
 * Created by pturpin on 21/05/2017.
 */
public class ConfiguredMethodTestRunner_UT {

  @Test
  public void testMethodConfiguration() {
    Set<RegistryFactory> registryFactories = ImmutableSet.of(
        new EmptyRegistryFactory(),
        new SpecialDoubleRegistryFactory(),
        new DefaultRegistryFactory());

    Set<RandomFactory> randomFactories = ImmutableSet.of(
        new RandomSeed1Factory(),
        new RandomSeed2Factory(),
        new DefaultRandomFactory());

    Set<Integer> nbRuns = ImmutableSet.of(1, 10, 50);
    Set<Double> acceptSkippeds = ImmutableSet.of(0.0, 0.25, 0.5, 0.75, 1.0);

    nbRuns.stream()
        .flatMap(nbRun -> acceptSkippeds.stream()
            .flatMap(acceptSkipped -> registryFactories.stream()
                .flatMap(registryFactory -> randomFactories.stream()
                    .map(randomFactory ->
                        TestRunnerConfigurations.configuration(nbRun, acceptSkipped, randomFactory, registryFactory)))))
        .forEach(ConfiguredTestRunner.unchecked(ConfiguredMethodTestRunner_UT::checkMethodConfiguration));
  }

  private static void checkMethodConfiguration(TestRunnerConfiguration config) throws Exception {
    updateAnnotationValue(config);

    Optional<Generator<Double>> optDoubleGenerator = config.getRegistryFactory()
        .create()
        .lookup(new ClassIdentifier<>(double.class));
    Random random = config.getRandomFactory().create();

    boolean areExpectingError = !optDoubleGenerator.isPresent() || config.getNbRun() * config.acceptSkipped() <= 1 || config.acceptSkipped() <= 0.5;

    UnitTest.state = new State();
    UnitTest.skipCounter = 0;
    JUnitCore junit = new JUnitCore();
    Result result = junit.run(UnitTest.class);
    State state = UnitTest.state;

    if (optDoubleGenerator.isPresent()) {
      Assert.assertEquals(config.getNbRun(), state.counter);

      Generator<Double> doubleGenerator = optDoubleGenerator.get();
      Assert.assertTrue(state.firstDoubleInitialized);
      Assert.assertEquals(doubleGenerator.get(random), state.firstDouble, 0);
    } else {
      Assert.assertFalse(state.firstDoubleInitialized);
    }

    Assert.assertEquals(areExpectingError, !result.wasSuccessful());
  }

  @Test
  public void testUpdatingAnnotation() throws Exception {
    TestRunnerConfiguration baseConfig = TestRunnerConfigurations.defaultConfiguration();
    Method method = UnitTest.class.getDeclaredMethod("test", double.class);

    TestRunnerConfiguration settedConfig = TestRunnerConfigurations.defaultConfiguration();
    updateAnnotationValue(settedConfig);
    TestRunnerConfiguration config = TestRunnerConfigurations.reflectiveMethodMapper(method).map(baseConfig);

    Assert.assertEquals(settedConfig.getNbRun(), config.getNbRun());
    Assert.assertEquals(settedConfig.acceptSkipped(), config.acceptSkipped(), 0);

    settedConfig = TestRunnerConfigurations.configuration(settedConfig.getNbRun() + 1,
        1 - settedConfig.acceptSkipped(), settedConfig.getRandomFactory(), settedConfig.getRegistryFactory());

    Assert.assertNotEquals(settedConfig.getNbRun(), config.getNbRun());
    Assert.assertNotEquals(settedConfig.acceptSkipped(), config.acceptSkipped(), 0);

    updateAnnotationValue(settedConfig);
    config = TestRunnerConfigurations.reflectiveMethodMapper(method).map(baseConfig);

    Assert.assertEquals(settedConfig.getNbRun(), config.getNbRun());
    Assert.assertEquals(settedConfig.acceptSkipped(), config.acceptSkipped(), 0);
  }

  @RunWith(RandomRunner.class)
  public static final class UnitTest {
    static State state = new State();
    static int skipCounter = 0;

    @Test
    @TestConfiguration.NbRun(1)
    @TestConfiguration.Skipped(0.0)
    @TestConfiguration.Random(DefaultRandomFactory.class)
    @TestConfiguration.Registry(DefaultRegistryFactory.class)
    public TestResult test(double v) {
      state.counter++;
      if (!state.firstDoubleInitialized) {
        state.firstDoubleInitialized = true;
        state.firstDouble = v;
      }
      return when(++skipCounter % 2 == 0, () -> {});
    }
  }

  private static void updateAnnotationValue(TestRunnerConfiguration config) throws Exception {
    Class<?> klass = UnitTest.class;
    Method testMethod = klass.getDeclaredMethod("test", double.class);
    Method method = Executable.class.getDeclaredMethod("declaredAnnotations");
    method.setAccessible(true);
    Map<Class<? extends Annotation>, Annotation> annotationsMap = (Map) method.invoke(testMethod);

    TestConfiguration.NbRun oldNbRunAnnot = (TestConfiguration.NbRun) annotationsMap.get(TestConfiguration.NbRun.class);
    TestConfiguration.Skipped oldSkippedAnnot = (TestConfiguration.Skipped) annotationsMap.get(TestConfiguration.Skipped.class);
    TestConfiguration.Random oldRandomAnnot = (TestConfiguration.Random) annotationsMap.get(TestConfiguration.Random.class);
    TestConfiguration.Registry oldRegistryAnnot = (TestConfiguration.Registry) annotationsMap.get(TestConfiguration.Registry.class);

    TestConfiguration.NbRun newNbRunAnnot = new TestConfiguration.NbRun() {
      @Override public Class<? extends Annotation> annotationType() {
        return oldNbRunAnnot.annotationType();
      }
      @Override public long value() {
        return config.getNbRun();
      }
      @Override public int hashCode() {
        return oldNbRunAnnot.hashCode();
      }
      @Override public boolean equals(Object o) {
        return oldNbRunAnnot.equals(o);
      }
      @Override public String toString() {
        return oldNbRunAnnot.toString();
      }
    };

    TestConfiguration.Skipped newSkippedAnnot = new TestConfiguration.Skipped() {
      @Override public Class<? extends Annotation> annotationType() {
        return oldSkippedAnnot.annotationType();
      }
      @Override public double value() {
        return config.acceptSkipped();
      }
      @Override public int hashCode() {
        return oldSkippedAnnot.hashCode();
      }
      @Override public boolean equals(Object o) {
        return oldSkippedAnnot.equals(o);
      }
      @Override public String toString() {
        return oldSkippedAnnot.toString();
      }
    };

    TestConfiguration.Random newRandomAnnot = new TestConfiguration.Random() {
      @Override public Class<? extends Annotation> annotationType() {
        return oldRandomAnnot.annotationType();
      }
      @Override public Class<? extends RandomFactory> value() {
        return config.getRandomFactory().getClass();
      }
      @Override public int hashCode() {
        return oldRandomAnnot.hashCode();
      }
      @Override public boolean equals(Object o) {
        return oldRandomAnnot.equals(o);
      }
      @Override public String toString() {
        return oldRandomAnnot.toString();
      }
    };

    TestConfiguration.Registry newRegistryAnnot = new TestConfiguration.Registry() {
      @Override public Class<? extends Annotation> annotationType() {
        return oldRegistryAnnot.annotationType();
      }
      @Override public Class<? extends RegistryFactory> value() {
        return config.getRegistryFactory().getClass();
      }
      @Override public int hashCode() {
        return oldRegistryAnnot.hashCode();
      }
      @Override public boolean equals(Object o) {
        return oldRegistryAnnot.equals(o);
      }
      @Override public String toString() {
        return oldRegistryAnnot.toString();
      }
    };

    annotationsMap.put(TestConfiguration.NbRun.class, newNbRunAnnot);
    annotationsMap.put(TestConfiguration.Skipped.class, newSkippedAnnot);
    annotationsMap.put(TestConfiguration.Random.class, newRandomAnnot);
    annotationsMap.put(TestConfiguration.Registry.class, newRegistryAnnot);
  }
}
