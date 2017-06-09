package fr.pturpin.quickcheck.test;

import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.ReflectiveGenerators;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.test.configuration.TestRunnerConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

  public static TestRunner randomRunner(Function<Object[], TestRunner> factory, Generator<Object[]> parametersGen, TestRunnerConfiguration configuration) throws NoRegisteredGenerator {
    Supplier<Random> randomFactory = configuration.getRandomFactory()::create;
    TestRunner randomRunner = new RandomTestRunner(factory, configuration.getNbRun(), parametersGen, randomFactory);
    TestRunner runner = namedRunner("Randomized(" + factory + ")", randomRunner);
    return failingSkipped(configuration.acceptSkipped(), runner);
  }

  public static TestRunner randomRunner(Method method, Function<Object[], TestRunner> factory, TestRunnerConfiguration configuration) throws NoRegisteredGenerator {
    Generator<Object[]> parametersGen = fetchParametersGen(method, configuration);
    Supplier<Random> randomFactory = configuration.getRandomFactory()::create;
    TestRunner randomRunner = new RandomTestRunner(factory, configuration.getNbRun(), parametersGen, randomFactory);
    TestRunner runner = namedRunner("Randomized(" + method.getName() + ")", randomRunner);
    return failingSkipped(configuration.acceptSkipped(), runner);
  }

  /**
   * Fetch available parameters generator for given method using given configuration.
   * If no generator are available, a {@link NoRegisteredGenerator} exception is thrown.
   *
   * @see ReflectiveGenerators#parametersGen(Method)
   *
   * @param method method to inspect
   * @param configuration configured registry to use
   * @return generator of arguments of given method
   * @throws NoRegisteredGenerator
   */
  private static Generator<Object[]> fetchParametersGen(Method method, TestRunnerConfiguration configuration) throws NoRegisteredGenerator {
    Registry configRegistry = configuration.getRegistryFactory().create();
    Registry klassRegistry = Registries.forClass(method.getDeclaringClass());
    Registry registry = Registries.alternatives(klassRegistry, configRegistry);
    ReflectiveGenerators reflectiveGenerators = ReflectiveGenerators.with(registry);
    return reflectiveGenerators.parametersGen(method)
        .orElseThrow(() -> new NoRegisteredGenerator(method));
  }

  /**
   * Decorates given runner by transforming skipped test result into a failure if the skip rate is higher than the given threshold.
   *
   * @param rate acceptable skipped test rate (between 0 and 1)
   * @param runner delegate runner
   * @return runner failing when skipped ratio is above threshold
   * @throws IllegalArgumentException if given rate is not between 0 and 1
   * @throws NullPointerException if given delegate is null
   */
  public static TestRunner failingSkipped(double rate, TestRunner runner) {
    checkNotNull(runner);
    checkArgument(rate >= 0 && rate <= 1);
    return namedRunner("FailingSkipped(" + runner + ")", () -> {
      TestResult result = runner.run();
      if (result.getState() == TestResult.TestState.FAILURE) {
        return result;
      }
      long allowedSkipped = (long) Math.ceil(rate * result.getNbTotal());
      if ((rate == 0. && result.getNbSkipped() > 0) || (rate != 0. && result.getNbSkipped() >= allowedSkipped)) {
        return TestResult.failure(new SkippedTestError(runner.toString(), result));
      }
      return result;
    });
  }

  /**
   * Returns a new named runner from the given runner.
   * The name is used as result of {@link Object#toString()}.
   *
   * The returned runner is a view on the given one.
   * So any potential modification one the given are done on this one.
   *
   * @param name name of the new runner
   * @param runner delegate runner to name
   * @return new named runner
   * @throws NullPointerException if name or runner are null
   */
  public static TestRunner namedRunner(String name, TestRunner runner) {
    return new NamedTestRunner(runner, name);
  }

  /**
   * Creates a new runner factory from a given public static method.
   *
   * The method may return void or {@link TestResult}. In that last case, the returned result is used as runner output.
   * Else, the result is ok if no exception was thrown during the invocation of the method, or a failure with the thrown exception.
   * Note that a method returning a {@link TestResult} and throwing exception is also considered as a failure test.
   *
   * The returned factory expect the method parameters in an {@link Object} array as {@link Method#invoke(Object, Object...)}.
   * So an {@link IllegalArgumentException} may be thrown at invocation if the factory was not complete with appropriate parameters.
   *
   * @see #methodRunner(Method, Object)
   *
   * @param method method to invoke
   * @return runner factory waiting method parameters
   * @throws IllegalArgumentException if the method isn't public static
   *    or if the method return type is neither void or {@link TestResult}.
   * @throws NullPointerException if given method is null
   */
  public static Function<Object[], TestRunner> staticMethodRunner(Method method) {
    return methodRunner(method, null);
  }

  /**
   * Creates a new runner factory from a given public method and its object instance (null if the method is static).
   *
   * The method may return void or {@link TestResult}. In that last case, the returned result is used as runner output.
   * Else, the result is ok if no exception was thrown during the invocation of the method, or a failure with the thrown exception.
   * Note that a method returning a {@link TestResult} and throwing exception is also considered as a failure test.
   *
   * The returned factory expect the method parameters in an {@link Object} array as {@link Method#invoke(Object, Object...)}.
   * So an {@link IllegalArgumentException} may be thrown at invocation if the factory was not complete with appropriate parameters.
   *
   * @param method method to invoke
   * @param instance potential object instance if method is not static
   * @return runner factory waiting method parameters
   * @throws IllegalArgumentException if the method isn't public
   *    or if the method is static but the given instance isn't null
   *    or if the method is not static but the given instance is null
   *    or if the method return type is neither void or {@link TestResult}.
   * @throws NullPointerException if given method is null
   */
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

  public static final class NoRegisteredGenerator extends Exception {
    NoRegisteredGenerator(Method method) {
      super("No registered generator for all parameters of " + method);
    }
  }

  private static final class SkippedTestError extends AssertionError {
    SkippedTestError(String testName, TestResult result) {
      super("Test " + testName + " was skipped " + result.getNbSkipped() + " times on a total of " + result.getNbTotal() + " runs");
    }
  }
}
