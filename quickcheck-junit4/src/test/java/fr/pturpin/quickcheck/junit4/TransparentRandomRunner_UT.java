package fr.pturpin.quickcheck.junit4;

import fr.pturpin.quickcheck.test.TestResult;
import fr.pturpin.quickcheck.test.configuration.TestConfiguration;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

import java.util.stream.Collectors;

/**
 * Created by pturpin on 15/05/2017.
 */
@RunWith(RandomRunner.class)
@TestConfiguration(nbRun = TransparentRandomRunner_UT.NB_RUN)
public class TransparentRandomRunner_UT {

  static final int NB_RUN = 10;
  private static int nonVoidTestMethodShouldBeCalledAsConfiguredCounter = 0;
  private static int testMethodWithParametersShouldBeCalledAsConfiguredCounter = 0;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void voidTestMethodWithoutParametersShouldBeCalledOnceAsNormalJUnitTest() {
    NotConfiguredTestShouldBeCalledOnceAsNormalJUnitTest.wasInvoked = false;
    JUnitCore junit = new JUnitCore();
    Result result = junit.run(NotConfiguredTestShouldBeCalledOnceAsNormalJUnitTest.class);
    Assert.assertTrue(result.wasSuccessful());
  }

  public static final class NotConfiguredTestShouldBeCalledOnceAsNormalJUnitTest {
    private static boolean wasInvoked = false;
    @Test public void test() {
      Assert.assertFalse(wasInvoked);
      wasInvoked = true;
    }
  }

  @Test
  public TestResult nonVoidTestMethodShouldBeCalledAsConfigured() {
    nonVoidTestMethodShouldBeCalledAsConfiguredCounter++;
    return TestResult.ok();
  }

  @Test
  public void testMethodWithParametersShouldBeCalledAsConfigured(double dummy) {
    testMethodWithParametersShouldBeCalledAsConfiguredCounter++;
  }

  @AfterClass
  public static void checkCounter() {
    Assert.assertEquals(NB_RUN, nonVoidTestMethodShouldBeCalledAsConfiguredCounter);
    Assert.assertEquals(NB_RUN, testMethodWithParametersShouldBeCalledAsConfiguredCounter);
  }

  @Test(timeout=20)
  public TestResult timeoutShouldBeOnEachCallAndNotAllCall() throws InterruptedException {
    Thread.sleep(10);
    return TestResult.ok();
  }

  @Test(expected=MyException.class)
  public TestResult expectedShouldPreventFailure() throws Exception {
    throw new MyException();
  }

  @Test(expected=MyException.class)
  public TestResult expectedShouldPreventFailureAsTestResult() throws Exception {
    return TestResult.failure(new MyException());
  }

  @Test
  public TestResult ruleShouldPreventFailure() throws Exception {
    exception.expect(MyException.class);
    throw new MyException();
  }

  @Test
  public TestResult ruleShouldPreventFailureAsTestResult() throws Exception {
    exception.expect(MyException.class);
    return TestResult.failure(new MyException());
  }

  @Test
  @TestConfiguration.NbRun(1)
  public void afterAndBeforeClassShouldBeCalled() throws InitializationError {
    JUnitCore junit = new JUnitCore();

    Assert.assertEquals(0, BeforeAndAfterShouldBeCalled.counterClass);
    Result result = junit.run(BeforeAndAfterShouldBeCalled.class);
    if (!result.getFailures().isEmpty()) {
      throw new InitializationError(result.getFailures().stream()
          .map(Failure::getException)
          .collect(Collectors.toList()));
    }
    Assert.assertEquals(3, BeforeAndAfterShouldBeCalled.counterClass);
  }

  @RunWith(RandomRunner.class)
  public static final class BeforeAndAfterShouldBeCalled {
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
    public TestResult beforeAndAfterShouldBeCalled() {
      Assert.assertTrue(counter > 0);
      Assert.assertTrue(counter % 3 == 1);
      Assert.assertTrue((counterClass == 1 && counter == 1) || (counter >= 4 && counterClass == 2));
      counterClass = 2;
      counter++;
      return TestResult.ok();
    }
  }

  private static final class MyException extends Exception {
  }
}
