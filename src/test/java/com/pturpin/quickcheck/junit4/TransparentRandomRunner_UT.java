package com.pturpin.quickcheck.junit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Created by pturpin on 15/05/2017.
 */
@RunWith(RandomRunner.class)
public class TransparentRandomRunner_UT {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test(timeout=1000)
  public void timeoutShouldBeOnEachCallAndNotAllCall() throws InterruptedException {
    Thread.sleep(10);
  }

  @Test(expected=MyException.class)
  public void shouldNotThrow() throws Exception {
    throw new MyException();
  }

  @Test
  public void ruleShouldPreventFailure() throws Exception {
    exception.expect(MyException.class);
    throw new MyException();
  }

  private static final class MyException extends Exception {
  }
}
