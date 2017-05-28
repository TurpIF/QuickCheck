package fr.pturpin.quickcheck.sample;

import fr.pturpin.quickcheck.annotation.Doubles;
import fr.pturpin.quickcheck.annotation.Ints;
import fr.pturpin.quickcheck.annotation.Nullable;
import fr.pturpin.quickcheck.junit4.QuickCheck;
import fr.pturpin.quickcheck.test.TestResult;
import fr.pturpin.quickcheck.test.configuration.TestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static fr.pturpin.quickcheck.test.TestResult.when;

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
    return when(value != null, () -> {
      double parsed = Double.parseDouble(value.toString());
      Assert.assertEquals(value, parsed, 0.d);
    });
  }

  @Test
  public void absoluteValueWithoutBranchingShouldBeEqualToMathAbs(int value) {
    Assert.assertEquals(Math.abs(value), myAbs(value));
  }

  @Test
  public TestResult absoluteValueShouldBePositive(@Ints.Extra int value) {
    return when(value != Integer.MIN_VALUE, () -> {
      Assert.assertTrue(myAbs(value) >= 0);
    });
  }

  private static int myAbs(int value) {
    int mask = value >> (Integer.SIZE - 1);
    return (value + mask) ^ mask;
  }
}
