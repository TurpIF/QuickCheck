package com.pturpin.quickcheck.junit4;

import com.pturpin.quickcheck.test.TestResult;
import com.pturpin.quickcheck.test.TestRunner;
import com.pturpin.quickcheck.test.TestRunners;
import com.pturpin.quickcheck.test.configuration.TestRunnerConfiguration;
import com.pturpin.quickcheck.test.configuration.TestRunnerConfigurations;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by turpif on 28/04/17.
 */
public class RandomRunner extends BlockJUnit4ClassRunner {

  private final TestRunnerConfiguration configuration;

  public RandomRunner(Class<?> klass) throws InitializationError {
    super(klass);

    try {
      this.configuration = TestRunnerConfigurations.reflectiveConfiguration(klass)
          .orElseGet(TestRunnerConfigurations::defaultConfiguration);
    } catch (ReflectiveOperationException e) {
      throw new InitializationError(e);
    }
  }

  RandomRunner(Class<?> klass, TestRunnerConfiguration configuration) throws InitializationError {
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
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    Method reflectMethod = method.getMethod();
    TestRunner runner;
    try {
      TestRunnerConfiguration methodConfiguration = TestRunnerConfigurations.reflectiveMethodConfiguration(reflectMethod, this.configuration);
      runner = TestRunners.randomRunner(reflectMethod, test, methodConfiguration);
    } catch (ReflectiveOperationException | TestRunners.NoRegisteredGenerator e) {
      return new Fail(e);
    }

    return LambdaStatement.of(() -> {
      TestResult result = runner.run();
      if (result.getFailureCause().isPresent()) {
        throw result.getFailureCause().get();
      }
    });
  }
}
