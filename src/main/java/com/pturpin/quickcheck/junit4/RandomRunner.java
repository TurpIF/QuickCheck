package com.pturpin.quickcheck.junit4;

import com.pturpin.quickcheck.base.Reflections;
import com.pturpin.quickcheck.generator.Generator;
import com.pturpin.quickcheck.generator.ReflectiveGenerators;
import com.pturpin.quickcheck.registry.Registry;
import com.pturpin.quickcheck.test.TestResult;
import com.pturpin.quickcheck.test.TestRunner;
import com.pturpin.quickcheck.test.TestRunners;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.*;

/**
 * Created by turpif on 28/04/17.
 */
public class RandomRunner extends BlockJUnit4ClassRunner {

  private static final int DEFAULT_NB_RUN = 100;
  private static final boolean DEFAULT_ACCEPT_SKIPPED = false;
  private static final Class<? extends RandomFactory> DEFAULT_RANDOM_FACTORY = DefaultRandomFactory.class;
  private static final Class<? extends RegistryFactory> DEFAULT_REGISTRY_FACTORY = DefaultRegistryFactory.class;

  private final ReflectiveGenerators generators;
  private final long nbRun;
  private final Supplier<Random> random;
  private final boolean acceptSkipped;

  public RandomRunner(Class<?> klass) throws InitializationError {
    super(klass);

    RandomRunnerConfiguration configuration = klass.getAnnotation(RandomRunnerConfiguration.class);

    Class<? extends RandomFactory> randomKlass;
    Class<? extends RegistryFactory> registryKlass;

    if (configuration == null) {
      this.nbRun = DEFAULT_NB_RUN;
      this.acceptSkipped = DEFAULT_ACCEPT_SKIPPED;
      randomKlass = DEFAULT_RANDOM_FACTORY;
      registryKlass = DEFAULT_REGISTRY_FACTORY;
    } else {
      this.nbRun = configuration.nbRun();
      this.acceptSkipped = configuration.acceptSkipped();
      randomKlass = configuration.random();
      registryKlass = configuration.registry();
    }

    checkState(this.nbRun > 0);

    RandomFactory randomFactory = newFactory(randomKlass);
    this.random = randomFactory::create;
    this.generators = ReflectiveGenerators.with(newFactory(registryKlass).create());

    validateGeneratorErrors();
  }

  RandomRunner(Registry registry, Class<?> klass, long nbRun, Supplier<Random> random, boolean acceptSkipped) throws InitializationError {
    super(klass);
    checkArgument(nbRun > 0);
    this.generators = ReflectiveGenerators.with(registry);
    this.nbRun = nbRun;
    this.random = checkNotNull(random);
    this.acceptSkipped = acceptSkipped;

    validateGeneratorErrors();
  }

  private static <T> T newFactory(Class<T> klass) throws InitializationError {
    try {
      return Reflections.newFactory(klass);
    } catch (ReflectiveOperationException e) {
      throw new InitializationError(e);
    }
  }

  private void validateGeneratorErrors() throws InitializationError{
    List<Throwable> errors = new ArrayList<>();
    getTestClass().getAnnotatedMethods(Test.class)
        .forEach(method -> validateParametersInRegistry(errors, method));

    if (!errors.isEmpty()) {
      throw new InitializationError(errors);
    }
  }

  private void validateParametersInRegistry(List<Throwable> errors, FrameworkMethod method) {
    Arrays.stream(method.getMethod().getParameters())
        .filter(parameter -> !generators.parameterGen(parameter).isPresent())
        .forEach(parameter -> errors.add(new Exception(
            "No registered generator for parameter " + parameter + " in " + method)));
  }

  @Override
  protected void validateTestMethods(List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Test.class);

    for (FrameworkMethod method : methods) {
      String methodName = method.getName() + "(" + method + ")";
      if (method.isStatic()) {
        errors.add(new Exception("Method " + methodName + " should not be static"));
      }
      if (!method.isPublic()) {
        errors.add(new Exception("Method " + methodName + " should be public"));
      }
      if (method.getReturnType() != Void.TYPE && !method.getReturnType().isAssignableFrom(TestResult.class)) {
        errors.add(new Exception("Method " + methodName + " should be void or " + TestResult.class));
      }
    }
  }

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    Object test;
    try {
      test = new ReflectiveCallable() {
        @Override
        protected Object runReflectiveCall() throws Throwable {
          return createTest();
        }
      }.run();
    } catch (Throwable e) {
      return new Fail(e);
    }

    return methodInvoker(method, test);
  }

  @Override
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    Method reflectMethod = method.getMethod();
    Generator<Object[]> parametersGen = generators.parametersGen(reflectMethod).get();
    Function<Object[], TestRunner> runnerFactory = TestRunners.methodRunner(reflectMethod, test);
    Function<Object[], TestRunner> decoratedRunnerFactory = decorateRunnerFactory(method, test, runnerFactory);

    // "Normal" test methods are simply executed once as a normal junit test
    long methodNbRun = nbRun;
    if (void.class.equals(method.getReturnType()) && method.getMethod().getParameterCount() == 0) {
      methodNbRun = 1;
    }
    TestRunner runner = TestRunners.randomRunner(decoratedRunnerFactory, parametersGen, methodNbRun, random);

    return LambdaStatement.of(() -> {
      TestResult result = runner.run();
      if (result.getFailureCause().isPresent()) {
        throw result.getFailureCause().get();
      }
      if (!acceptSkipped && TestResult.TestState.SKIPPED.equals(result.getState())) {
        throw new SkippedTestError(reflectMethod);
      }
    });
  }

  private Function<Object[], TestRunner> decorateRunnerFactory(FrameworkMethod method, Object test, Function<Object[], TestRunner> factory) {
    return parameters -> {
      TestRunner testRunner = factory.apply(parameters);
      TestResult[] result = new TestResult[]{ TestResult.failure(new IllegalStateException("Test result was not set")) };
      boolean[] hasThrow = new boolean[]{ false };

      Statement decoratedStatement = decorateStatement(method, test, LambdaStatement.of(() -> {
        result[0] = testRunner.run();
        if (result[0].getFailureCause().isPresent()) {
          hasThrow[0] = true;
          throw result[0].getFailureCause().get();
        }
      }));

      return () -> {
        try {
          decoratedStatement.evaluate();
        } catch (Throwable t) {
          return TestResult.failure(t);
        }
        if (hasThrow[0]) {
          return TestResult.ok();
        }
        return result[0];
      };
    };
  }

  private Statement decorateStatement(FrameworkMethod method, Object test, Statement statement) {
    statement = possiblyExpectingExceptions(method, test, statement);
    statement = withPotentialTimeout(method, test, statement);
    statement = withBefores(method, test, statement);
    statement = withAfters(method, test, statement);
    statement = withRules(method, test, statement);
    return statement;
  }

  /** Copy of BlockJUnit4ClassRunner to reproduce rule feature **/

  private Statement withRules(FrameworkMethod method, Object target, Statement statement) {
    List<TestRule> testRules = getTestRules(target);
    Statement result = statement;
    result = withMethodRules(method, testRules, target, result);
    result = withTestRules(method, testRules, result);

    return result;
  }

  private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules, Object target, Statement result) {
    for (org.junit.rules.MethodRule each : getMethodRules(target)) {
      if (!testRules.contains(each)) {
        result = each.apply(result, method, target);
      }
    }
    return result;
  }

  private List<org.junit.rules.MethodRule> getMethodRules(Object target) {
    return rules(target);
  }

  private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules, Statement statement) {
    return testRules.isEmpty() ? statement :
        new RunRules(statement, testRules, describeChild(method));
  }

  /** End of copy of BlockJUnit4ClassRunner **/

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface RandomRunnerConfiguration {
    int nbRun() default DEFAULT_NB_RUN;
    boolean acceptSkipped() default DEFAULT_ACCEPT_SKIPPED;
    Class<? extends RandomFactory> random() default DefaultRandomFactory.class;
    Class<? extends RegistryFactory> registry() default DefaultRegistryFactory.class;
  }

  public interface RandomFactory {
    Random create();
  }

  public interface RegistryFactory {
    Registry create();
  }

  private static final class DefaultRandomFactory implements RandomFactory {

    public DefaultRandomFactory() {
      // nothing
    }

    @Override
    public Random create() {
      return new Random(0L);
    }
  }

  private static final class SkippedTestError extends AssertionError {
    SkippedTestError(Method method) {
      super("Method test " + method + " was skipped");
    }
  }
}
