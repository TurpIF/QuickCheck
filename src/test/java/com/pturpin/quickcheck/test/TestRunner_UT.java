package com.pturpin.quickcheck.test;

import com.pturpin.quickcheck.test.TestResult.TestState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.function.Function;

import static com.pturpin.quickcheck.assertion.Assertions.assertThrow;

/**
 * Created by turpif on 21/05/17.
 */
public class TestRunner_UT {

  private Method privateStaticVoidMethod;
  private Method packageStaticVoidMethod;
  private Method publicStaticNonVoidMethod;
  private Method publicStaticVoidMethod;
  private Method publicStaticTestResultMethod;
  private Method privateVoidMethod;
  private Method packageVoidMethod;
  private Method publicNonVoidMethod;
  private Method publicVoidMethod;
  private Method publicTestResultMethod;
  private Method baseMethod;
  private Method extensionMethod;
  private Method publicStaticVoidThrowingMethod;
  private Method publicStaticTestResultThrowingMethod;
  private Method publicStaticTestResultReturningFailureMethod;
  private Method publicStaticTestResultReturningOkMethod;
  private Method publicStaticTestResultSkippedMethod;

  @Before
  public void before() throws Exception {
    privateStaticVoidMethod = TestRunner_UT.class.getDeclaredMethod("privateStaticVoidMethod");
    packageStaticVoidMethod = TestRunner_UT.class.getDeclaredMethod("packageStaticVoidMethod");
    publicStaticNonVoidMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticNonVoidMethod");
    publicStaticVoidMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticVoidMethod");
    publicStaticTestResultMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticTestResultMethod");
    privateVoidMethod = TestRunner_UT.class.getDeclaredMethod("privateVoidMethod");
    packageVoidMethod = TestRunner_UT.class.getDeclaredMethod("packageVoidMethod");
    publicNonVoidMethod = TestRunner_UT.class.getDeclaredMethod("publicNonVoidMethod");
    publicVoidMethod = TestRunner_UT.class.getDeclaredMethod("publicVoidMethod");
    publicTestResultMethod = TestRunner_UT.class.getDeclaredMethod("publicTestResultMethod");
    baseMethod = Base.class.getDeclaredMethod("methodBase");
    extensionMethod = Extension.class.getDeclaredMethod("methodExtension");
    publicStaticVoidThrowingMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticVoidThrowingMethod");
    publicStaticTestResultThrowingMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticTestResultThrowingMethod");
    publicStaticTestResultReturningFailureMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticTestResultReturningFailureMethod");
    publicStaticTestResultReturningOkMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticTestResultReturningOkMethod");
    publicStaticTestResultSkippedMethod = TestRunner_UT.class.getDeclaredMethod("publicStaticTestResultSkippedMethod");
  }

  @Test
  public void staticMethodRunnerShouldOnlyAcceptPublicStaticVoidOrTestResultMethod() {
    assertThrow(() -> TestRunners.staticMethodRunner(privateStaticVoidMethod));
    assertThrow(() -> TestRunners.staticMethodRunner(packageStaticVoidMethod));
    assertThrow(() -> TestRunners.staticMethodRunner(publicStaticNonVoidMethod));
    TestRunners.staticMethodRunner(publicStaticVoidMethod);
    TestRunners.staticMethodRunner(publicStaticTestResultMethod);
    assertThrow(() -> TestRunners.staticMethodRunner(privateVoidMethod));
    assertThrow(() -> TestRunners.staticMethodRunner(packageVoidMethod));
    assertThrow(() -> TestRunners.staticMethodRunner(publicNonVoidMethod));
    assertThrow(() -> TestRunners.staticMethodRunner(publicVoidMethod));
    assertThrow(() -> TestRunners.staticMethodRunner(publicTestResultMethod));
  }

  @Test
  public void instanceMethodRunnerShouldOnlyAcceptPublicVoidOrTestResultMethod() {
    assertThrow(() -> TestRunners.methodRunner(privateStaticVoidMethod, this));
    assertThrow(() -> TestRunners.methodRunner(packageStaticVoidMethod, this));
    assertThrow(() -> TestRunners.methodRunner(publicStaticNonVoidMethod, this));
    assertThrow(() -> TestRunners.methodRunner(publicStaticVoidMethod, this));
    assertThrow(() -> TestRunners.methodRunner(publicStaticTestResultMethod, this));
    assertThrow(() -> TestRunners.methodRunner(privateVoidMethod, this));
    assertThrow(() -> TestRunners.methodRunner(packageVoidMethod, this));
    assertThrow(() -> TestRunners.methodRunner(publicNonVoidMethod, this));
    TestRunners.methodRunner(publicVoidMethod, this);
    TestRunners.methodRunner(publicTestResultMethod, this);
  }

  @Test
  public void instanceMethodRunnerShouldOnlyAcceptInstanceOfClassDeclaringTheMethod() {
    Base base = new Base();
    Extension extension = new Extension();

    TestRunners.methodRunner(publicVoidMethod, this);
    assertThrow(() -> TestRunners.methodRunner(publicVoidMethod, base));
    assertThrow(() -> TestRunners.methodRunner(publicVoidMethod, extension));

    TestRunners.methodRunner(baseMethod, base);
    assertThrow(() -> TestRunners.methodRunner(baseMethod, this));
    assertThrow(() -> TestRunners.methodRunner(baseMethod, extension));

    TestRunners.methodRunner(extensionMethod, extension);
    assertThrow(() -> TestRunners.methodRunner(extensionMethod, this));
    assertThrow(() -> TestRunners.methodRunner(extensionMethod, base));
  }

  @Test
  public void testRunnerOfVoidMethodShouldReturnOkIfNoExceptionWasThrown() {
    assertResultEquals(publicStaticVoidMethod, TestState.OK);
  }

  @Test
  public void testRunnerOfVoidMethodShouldReturnFailureIfExceptionWasThrown() {
    assertResultEquals(publicStaticVoidThrowingMethod, TestState.FAILURE);
  }

  @Test
  public void testRunnerOfTestResultMethodShouldReturnFailureIfExceptionWasThrown() {
    assertResultEquals(publicStaticTestResultThrowingMethod, TestState.FAILURE);
  }

  @Test
  public void testRunnerOfTestResultMethodShouldReturnResult() {
    assertResultEquals(publicStaticTestResultReturningOkMethod, TestState.OK);
    assertResultEquals(publicStaticTestResultSkippedMethod, TestState.SKIPPED);
    assertResultEquals(publicStaticTestResultReturningFailureMethod, TestState.FAILURE);
  }

  private static class Base {
    public void methodBase() {
    }
  }

  private static final class Extension extends Base {
    public void methodExtension() {
    }
  }

  private static void privateStaticVoidMethod() {
  }

  static void packageStaticVoidMethod() {
  }

  public static String publicStaticNonVoidMethod() {
    return null;
  }

  private void privateVoidMethod() {
  }

  void packageVoidMethod() {
  }

  public String publicNonVoidMethod() {
    return null;
  }

  public static void publicStaticVoidMethod() {
  }

  public void publicVoidMethod() {
  }

  public static TestResult publicStaticTestResultMethod() {
    return null;
  }

  public TestResult publicTestResultMethod() {
    return null;
  }

  public static void publicStaticVoidThrowingMethod() throws MyException {
    throw new MyException();
  }

  public static TestResult publicStaticTestResultThrowingMethod() throws MyException {
    throw new MyException();
  }

  public static TestResult publicStaticTestResultReturningFailureMethod() {
    return TestResult.failure(new MyException());
  }

  public static TestResult publicStaticTestResultReturningOkMethod() {
    return TestResult.ok();
  }

  public static TestResult publicStaticTestResultSkippedMethod() {
    return TestResult.skipped();
  }

  private static void assertResultEquals(Method method, TestState expected) {
    Function<Object[], TestRunner> factory = TestRunners.staticMethodRunner(method);
    TestRunner runner = factory.apply(new Object[0]);
    TestResult result = runner.run();
    Assert.assertEquals(expected, result.getState());
  }

  private static final class MyException extends Exception {
  }

}
