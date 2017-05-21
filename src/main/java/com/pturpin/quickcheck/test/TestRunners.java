package com.pturpin.quickcheck.test;

import com.pturpin.quickcheck.generator.Generator;
import com.pturpin.quickcheck.generator.ReflectiveGenerators;
import com.pturpin.quickcheck.registry.Registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by turpif on 27/04/17.
 */
public final class TestRunners {

  private TestRunners() { /* Factory class */ }

  public static TestRunner randomRunner(Method method, Object instance, Registry registry, long nbRun, Supplier<Random> random) {
    Optional<Generator<Object[]>> optGenerator = ReflectiveGenerators.with(registry).parametersGen(method);
    return optGenerator
        .map(parametersGen -> randomRunner(method, instance, parametersGen, nbRun, random))
        .orElseThrow(UnsupportedOperationException::new);
  }

  public static TestRunner randomRunner(Method method, Object instance, Generator<Object[]> parametersGen, long nbRun, Supplier<Random> random) {
    Function<Object[], TestRunner> factory = methodRunner(method, instance);
    RandomTestRunner randomRunner = new RandomTestRunner(factory, nbRun, parametersGen, random);
    return namedRunner("Randomized(" + method.getName() + ")", randomRunner);
  }

  public static TestRunner failingSkipped(TestRunner runner) {
    checkNotNull(runner);
    return namedRunner("FailingSkipped(" + runner + ")", () -> {
      TestResult result = runner.run();
      if (result.getState() == TestResult.TestState.SKIPPED) {
        return TestResult.failure(new SkippedTestError(runner.toString()));
      }
      return result;
    });
  }

  public static TestRunner namedRunner(String name, TestRunner runner) {
    return new NamedTestRunner(runner, name);
  }

  public static Function<Object[], TestRunner> staticMethodRunner(Method method) {
    return methodRunner(method, null);
  }

  public static Function<Object[], TestRunner> methodRunner(Method method, Object instance) {
    checkArgument(instance != null || Modifier.isStatic(method.getModifiers()),
        "Impossible to invoke the non-static method without instance: %s", method);
    checkArgument(instance == null || !Modifier.isStatic(method.getModifiers()),
        "Static methods should not be invoked with an instance object: %s", method);
    checkArgument(instance == null || instance.getClass().equals(method.getDeclaringClass()),
        "Instance class should be equal to declaring class of method: %s", method);
    checkArgument(Modifier.isPublic(method.getModifiers()),
        "Impossible to invoke the non-public method: %s", method);
    checkArgument(void.class.equals(method.getReturnType()) || TestResult.class.equals(method.getReturnType()),
        "Method %s should return either void or " + TestResult.class.getSimpleName(), method);

    Class<?> returnType = method.getReturnType();
    boolean useReturn = TestResult.class.isAssignableFrom(returnType);

    return arguments -> () -> {
      try {
        Object result = method.invoke(instance, arguments);
        if (useReturn) {
          return (TestResult) result;
        }
        return TestResult.ok();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        return TestResult.failure(e.getCause());
      }
    };
  }

  private static final class NamedTestRunner implements TestRunner {
    private final TestRunner delegate;
    private final String name;

    private NamedTestRunner(TestRunner delegate, String name) {
      this.delegate = checkNotNull(delegate);
      this.name = checkNotNull(name);
    }

    @Override
    public TestResult run() {
      return delegate.run();
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static final class SkippedTestError extends AssertionError {
    SkippedTestError(String testName) {
      super("Test " + testName + " was skipped");
    }
  }
}
