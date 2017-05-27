package fr.pturpin.quickcheck.sample;

import fr.pturpin.quickcheck.annotation.Doubles;
import fr.pturpin.quickcheck.annotation.Nullable;
import fr.pturpin.quickcheck.junit4.QuickCheck;
import fr.pturpin.quickcheck.test.TestResult;
import fr.pturpin.quickcheck.test.configuration.TestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by turpif on 28/04/17.
 */
@RunWith(QuickCheck.class)
public class QuickCheckSample_UT {

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
  @TestConfiguration.Skipped(0.75)
  public TestResult toStringThenParseShouldBeIdentity(@Nullable Double value) {
    return TestResult.when(value != null, () -> {
      double parsed = Double.parseDouble(value.toString());
      Assert.assertEquals(value, parsed, 0.d);
    });
  }
}
