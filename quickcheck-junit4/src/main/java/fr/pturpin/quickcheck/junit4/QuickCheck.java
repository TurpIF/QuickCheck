package fr.pturpin.quickcheck.junit4;

import fr.pturpin.quickcheck.test.TestResult;
import fr.pturpin.quickcheck.test.TestRunner;
import fr.pturpin.quickcheck.test.TestRunners;
import fr.pturpin.quickcheck.test.configuration.TestConfiguration;
import fr.pturpin.quickcheck.test.configuration.TestRunnerConfiguration;
import fr.pturpin.quickcheck.test.configuration.TestRunnerConfigurations;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

/**
 * Created by turpif on 28/04/17.
 */
public class QuickCheck extends BlockJUnit4ClassRunner {

  private final TestRunnerConfiguration configuration;

  public QuickCheck(Class<?> klass) throws InitializationError {
    super(klass);

    try {
      this.configuration = TestRunnerConfigurations.reflectiveConfiguration(klass)
          .orElseGet(TestRunnerConfigurations::defaultConfiguration);
    } catch (ReflectiveOperationException e) {
      throw new InitializationError(e);
    }
  }

  QuickCheck(Class<?> klass, TestRunnerConfiguration configuration) throws InitializationError {
    super(klass);
    this.configuration = configuration;
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
    TestRunnerConfiguration.TestConfigurationMapper methodMapper;
    TestRunner runner;
    try {
      methodMapper = TestRunnerConfigurations.reflectiveMethodMapper(reflectMethod);
    } catch (ReflectiveOperationException e) {
      return new Fail(e);
    }

    boolean isNormalMethod = void.class.equals(method.getReturnType()) && reflectMethod.getParameterCount() == 0;
    boolean isConfigured = test.getClass().isAnnotationPresent(TestConfiguration.class)
        || reflectMethod.isAnnotationPresent(TestConfiguration.NbRun.class)
        || reflectMethod.isAnnotationPresent(TestConfiguration.Skipped.class)
        || reflectMethod.isAnnotationPresent(TestConfiguration.Random.class)
        || reflectMethod.isAnnotationPresent(TestConfiguration.Registry.class);
    if (isNormalMethod && !isConfigured) {
      methodMapper = TestRunnerConfigurations.compose(methodMapper, TestRunnerConfigurations.withNbRun(1L));
    }

    Function<Object[], TestRunner> runnerFactory = TestRunners.methodRunner(reflectMethod, test);
    Function<Object[], TestRunner> decoratedRunnerFactory = decorateRunnerFactory(method, test, runnerFactory);
    TestRunnerConfiguration methodConfiguration = methodMapper.map(this.configuration);
    try {
      runner = TestRunners.randomRunner(reflectMethod, decoratedRunnerFactory, methodConfiguration);
    } catch (TestRunners.NoRegisteredGenerator e) {
      return new Fail(e);
    }

    return LambdaStatement.of(() -> {
      TestResult result = runner.run();
      if (result.getFailureCause().isPresent()) {
        throw result.getFailureCause().get();
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
}
