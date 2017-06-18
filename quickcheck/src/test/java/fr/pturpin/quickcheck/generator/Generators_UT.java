package fr.pturpin.quickcheck.generator;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by pturpin on 17/06/2017.
 */
public class Generators_UT {

  @Test
  public void coGeneratorShouldAlwaysProduceSameOutputGivenSameInput() {
    ImmutableList.of(0, 1, "Hello", new ArrayList<>(), Generators_UT.class)
        .forEach(Generators_UT::coGeneratorShouldAlwaysProduceSameOutputGivenSameInput);
  }

  private static void coGeneratorShouldAlwaysProduceSameOutputGivenSameInput(Object input) {
    Generator<Double> coGen = Generators.coGenerator(input, NumberGens.doubleGen());
    Random re = new Random(0);

    long nbDistinct = IntStream.range(0, 50)
        .mapToObj(i -> coGen.get(re))
        .distinct()
        .count();

    Assert.assertTrue(nbDistinct == 1);
  }

  @Test
  public void coGeneratorShouldRestoreState() {
    Generator<Double> doubleGen = NumberGens.doubleGen();
    Generator<Double> coGen = Generators.coGenerator(0, doubleGen);

    Random re = new Random(0);

    Double double1 = coGen.get(re);
    Double double2 = doubleGen.get(re);
    Double double3 = coGen.get(re);
    Double double4 = doubleGen.get(re);

    Assert.assertEquals(double1, double3);
    Assert.assertNotEquals(double2, double4); // May be equal but with very small proba
  }

}
