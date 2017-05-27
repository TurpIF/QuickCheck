package fr.pturpin.quickcheck.assertion;

import fr.pturpin.quickcheck.functional.Checked;
import org.junit.Assert;

/**
 * Created by turpif on 21/05/17.
 */
public final class Assertions {

  private Assertions() {
  }

  public static void assertThrow(Checked.CheckedSupplier<?, ?> supplier) {
    try {
      supplier.get();
      Assert.fail("Given supplier should throw but didn't");
    } catch (Exception ignored) {
    }
  }
}
