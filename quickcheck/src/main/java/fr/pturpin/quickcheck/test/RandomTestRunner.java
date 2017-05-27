package fr.pturpin.quickcheck.test;

import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.test.TestResult.TestState;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by turpif on 27/04/17.
 */
public class RandomTestRunner implements TestRunner {

  private final Function<Object[], TestRunner> runnerFactory;
  private final long nbRun;
  private final Generator<Object[]> argumentsGen;
  private final Supplier<Random> randomSupplier;

  RandomTestRunner(Function<Object[], TestRunner> runnerFactory, long nbRun, Generator<Object[]> argumentsGen,
                   Supplier<Random> randomSupplier) {
    checkArgument(nbRun > 0);
    this.runnerFactory = checkNotNull(runnerFactory);
    this.nbRun = nbRun;
    this.argumentsGen = checkNotNull(argumentsGen);
    this.randomSupplier = checkNotNull(randomSupplier);
  }

  @Override
  public TestResult run() {
    Random random = randomSupplier.get();
    TestResult result = TestResult.empty();

    for (long i = 0; i < nbRun; i++) {
      // Don't check if sizeof arguments is what runnerFactory expect.
      // If the generator produce erroneous input data it's not a test error, but a framework error.
      Object[] arguments = argumentsGen.get(random);
      TestRunner runner = runnerFactory.apply(arguments);

      // Runner should not throw any exception.
      TestResult status = runner.run();

      // End after the first failure
      if (TestState.FAILURE.equals(status.getState())) {
        return status;
      }
      result = TestResult.merge(result, status);
    }
    return result;
  }
}
