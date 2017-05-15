package com.pturpin.quickcheck.junit4;


import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

import java.util.stream.Collectors;

/**
 * Created by pturpin on 15/05/2017.
 */
public class AfterAndBeforeWithRandomRunner_UT {

  @Test
  public void test() throws InitializationError {
    JUnitCore junit = new JUnitCore();

    Assert.assertEquals(0, UnitTest.counterClass);
    Result result = junit.run(UnitTest.class);
    if (!result.getFailures().isEmpty()) {
      throw new InitializationError(result.getFailures().stream()
          .map(Failure::getException)
          .collect(Collectors.toList()));
    }
    Assert.assertEquals(3, UnitTest.counterClass);
  }

  @RunWith(RandomRunner.class)
  public static final class UnitTest {
    private static int counterClass = 0;
    private int counter = 0;

    @BeforeClass
    public static void beforeClass() {
      Assert.assertEquals(0, counterClass);
      counterClass = 1;
    }

    @AfterClass
    public static void afterClass() {
      Assert.assertEquals(2, counterClass);
      counterClass = 3;
    }

    @Before
    public void before() {
      Assert.assertTrue(counter % 3 == 0);
      counter++;
    }

    @After
    public void after() {
      Assert.assertTrue(counter % 3 == 2);
      counter++;
    }

    @Test
    public void beforeAndAfterShouldBeCalled() {
      Assert.assertTrue(counter > 0);
      Assert.assertTrue(counter % 3 == 1);
      Assert.assertTrue((counterClass == 1 && counter == 1) || (counter >= 4 && counterClass == 2));
      counterClass = 2;
      counter++;
    }
  }
}
