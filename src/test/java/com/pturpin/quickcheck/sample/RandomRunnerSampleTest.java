package com.pturpin.quickcheck.sample;

import com.pturpin.quickcheck.annotation.Doubles;
import com.pturpin.quickcheck.annotation.Nullable;
import com.pturpin.quickcheck.junit4.RandomRunner;
import com.pturpin.quickcheck.test.TestResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.pturpin.quickcheck.test.TestResult.when;

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
  public TestResult ceilValueShouldBeGreaterOrEqThanValueExceptNaN(@Doubles.Exclude({Double.NaN}) @Doubles.Extra double value) {
    if (Double.isNaN(value)) {
      return TestResult.failure(new UnsupportedOperationException("NaN was not excluded"));
    }
    Assert.assertTrue(Math.ceil(value) >= value);
    return TestResult.ok();
  }

  @Test
  public TestResult toStringThenParseShouldBeIdentity(@Nullable Double value) {
    return when(value != null, () -> {
      double parsed = Double.parseDouble(value.toString());
      Assert.assertEquals(value, parsed, 0.d);
    });
  }
}
