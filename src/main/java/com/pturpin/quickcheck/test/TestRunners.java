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

/**
 * Created by turpif on 27/04/17.
 */
public final class TestRunners {

  private TestRunners() { /* Factory class */ }

  public static TestRunner randomRunner(Method method, Registry registry, long nbRun, Supplier<Random> random) {
    Optional<Generator<Object[]>> optGenerator = ReflectiveGenerators.with(registry).parametersGen(method);
    return optGenerator
        .map(generator -> new RandomTestRunner(methodRunner(method), nbRun, optGenerator.get(), random))
        .orElseThrow(UnsupportedOperationException::new);
  }

  public static Function<Object[], TestRunner> methodRunner(Method method) {
    checkArgument(Modifier.isStatic(method.getModifiers()),
        "Impossible to invoke a non-static method without instance : %s", method);

    Class<?> returnType = method.getReturnType();
    boolean useReturn = TestResult.class.isAssignableFrom(returnType);

    return arguments -> () -> {
      try {
        Object result = method.invoke(null, arguments);
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
}
