package fr.pturpin.quickcheck.sample;

import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.junit4.QuickCheck;
import fr.pturpin.quickcheck.registry.Registry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

/**
 * Created by turpif on 08/06/17.
 */
@RunWith(QuickCheck.class)
public class EmbeddedGenerator_UT {

  @Test
  public void testMyIntegerIsBetween0And10(MyInteger myInteger) {
    Assert.assertTrue(myInteger.value >= 0 && myInteger.value <= 10);
  }

  @Test
  public void testMyComposite(MyComposite myComposite) {
    testMyIntegerIsBetween0And10(myComposite.myInteger);
    Assert.assertEquals(42.1337, myComposite.myDouble, 0);
  }

  @Gen
  public static Generator<Double> myDoubleGen() {
    return Generators.constGen(42.1337);
  }

  @Gen
  public static Generator<MyInteger> myIntegerGen() {
    return Generators.map(NumberGens.integerGen(Ranges.closed(0, 10)), MyInteger::new);
  }

  @Gen
  public static Optional<Generator<MyComposite>> myCompositeGen(Registry registry) {
    return registry.lookup(classId(double.class))
        .map(doubleGen -> re -> new MyComposite(myIntegerGen().get(re), doubleGen.get(re)));
  }

  private static final class MyInteger {
    public final int value;

    MyInteger(int value) {
      this.value = value;
    }
  }

  private static final class MyComposite {
    public final MyInteger myInteger;
    public final double myDouble;

    private MyComposite(MyInteger myInteger, double myDouble) {
      this.myInteger = myInteger;
      this.myDouble = myDouble;
    }
  }

}
