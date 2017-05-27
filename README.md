# QuickCheck

QuickCheck is a library for random testing of program properties.

The programmer provides a specification of the program, in the form of properties which functions should satisfy,
and QuickCheck then tests that the properties hold in a large number of randomly generated cases.

Specifications are expressed as Java tests, using generators defined in the QuickCheck library.

### Integration with JUnit 4


The QuickCheck library provide the `QuickCheck` custom JUnit runner to facilitate the integration with provided
generators. It can be declared on your test classes by using the `RunWith` annotation of junit :

```java
import fr.pturpin.quickcheck.junit4.QuickCheck;
import org.junit.runner.RunWith;

@RunWith(QuickCheck.class)
public class MyTest {
  
  // tests...
  
}
```

Note that you can add it, without any issues, on your existing test classes.
Indeed, the runner will execute the classic method tests as the default junit runner will do.

After adding the QuickCheck runner, you can now declare test methods with parameters.
At execution, those test methods will be called several times and
the QuickCheck framework will fill automatically the parameters base on available generators.

For instance, how to check a bit hacks is true for any integer (or long) :
````java
@RunWith(QuickCheck.class)
public class MyTest {
    @Test
    public void absoluteValueWithoutBranchingShouldBeEqualToMathAbs(int value) {
      Assert.assertEquals(Math.abs(value), myAbs(value));
    }
     
    private static int myAbs(int value) {
      int mask = value >> (Integer.SIZE - 1);
      return (value + mask) ^ mask;
    }
}
````

### Controlling generated values

On the above example, we verified that our custom absolute value is equal to the `java.lang.Math::abs`.
But if we look at the mathematics definition of absolute value, we may be more precise on our specification :
the absolute value of x is a non-negative value of x without regard to its sign.

To test the non-negativity of our implementation, we may define another property test :
````java
import fr.pturpin.quickcheck.annotation.Ints;
import fr.pturpin.quickcheck.test.TestResult;
import static fr.pturpin.quickcheck.test.TestResult.when;

@RunWith(QuickCheck.class)
public class MyTest {
  @Test
  public TestResult absoluteValueShouldBePositive(@Ints.Extra int value) {
    return when(value != Integer.MIN_VALUE, () -> {
      Assert.assertTrue(myAbs(value) >= 0);
    });
  }
}
````

This test let us show that our implementation (and also the java implementation)
is not able to compute an absolute value for `Integer.MIN_VALUE`
(because the bit representation of integer is not able to represent the positive of that value).

The `@Ints.Extra` indicate that the basic integer generator should be extended with some values
(`Integer.MIN_VALUE` and `Integer.MAX_VALUE` by default).
As our test could fail only one 1 element in the whole possibilities of integers,
we have to help the generator by hard-coding some special values.
 
In addition to `Ints.Extra`, there is also `Ints.Filter`, `Ints.Exclude`, `Ints.Range`.
These annotations also exist for longs and doubles.

### Skipping a test

In the previous example, the implementation was not able to fulfill the property for a special value.
So, a test method can return a `TestResult` object to indicate if the test was ok, if it has failed or if it was skipped.
The `when` helper function was used for that but, it's possible to handle manually the process.

A skipped test may be innocent like in that case.
But if a test is always skipped for all generated values, it may indicate a error in the generator or in the test.
So they may be reported in test results for information purpose or
may even be interpreted as a failure if the rate of skipped tests exceed a configured threshold.

This can be configured on class level by using the `TestConfiguration` annotation or
on method level with the `TestConfiguration.Skipped` annotation.

### Declaring custom generators

The choice of the generators is done in function of the declared parameter types.
If no generator is available for a declared type, an error will be raised before the execution of the method.
Thus, you can easily identity the missing generators without waiting the end of your tests.

It's not possible to provide a generic generator for every potentially existing objects even by reflections.
Some object classes have invariants and it is not possible to detect that at runtime.
But, every object is a composition of primitive elements following these invariants.
Then, if a generator does not exist for a particular object classes,
it's possible to define one as a composition of primitive generators.

Defining them is just implementing a functional interface.
Declaring them to the core library registry should be done at class level with the `TestConfiguration` annotation
or at method level with the `TestConfiguration.Registry` annotation:

````java
public class MyTest {
  @Test
  @Registry(MyCustomRegistry.class)
  public void myTest(int myValue) {
    assert myValue >= 0 && myValue <= 4;
  }
  
  private static class MyCustomRegistry implements RegistryFactory {
    
    MyCustomRegistry() {}
    
    public Registry create() {
      return Registries.builder()
          .put(new ClassIdentifier<>(int.class), oneOf(0, 1, 2, 3, 4))
          .build();
    }
  }
}
````

### Reproducibility of tests

Generators are functional interfaces producing a value given a random engine.
By default, the configured random engine is a `java.util.Random(0)`.
The fixed seed yield a reproducible tests suit.

You can configure the random engine factory, still at class level with `TestConfiguration` and
at method level with `TestConfiguration.Random`.

It's important to note that a new random engine is created for every test methods.
This guarantee the isolation of side effect on the engine.

### Integration with other test libraries

For the moment, there's only an adapter for JUnit4.
Therefore, the core library can still be used manually by fetching the test parameters through generators:
