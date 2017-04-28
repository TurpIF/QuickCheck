package com.pturpin.quickcheck.sample;

import com.pturpin.quickcheck.junit4.RandomRunner;
import com.pturpin.quickcheck.test.TestResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.pturpin.quickcheck.test.TestResult.guard;

/**
 * Created by turpif on 28/04/17.
 */
@RunWith(RandomRunner.class)
public class RandomRunnerSampleTest {

  @Test
  public void absoluteValueShouldBePositiveExceptNaN(double value) {
    if (!Double.isNaN(value)) {
      Assert.assertTrue(Math.abs(value) >= 0);
    }
  }

  @Test
  public TestResult ceilValueShouldBeGreaterOrEqThanValueExceptNaN(double value) {
    if (Double.isNaN(value)) {
      return TestResult.skipped();
    }
    Assert.assertTrue(Math.ceil(value) >= value);
    return TestResult.ok();
  }

  @Test
  public TestResult toStringThenParseShouldBeIdentity(Double value) {
    return guard(value == null, () -> {
      double parsed = Double.parseDouble(value.toString());
      Assert.assertEquals(value, parsed, 0.d);
    });
  }
}
