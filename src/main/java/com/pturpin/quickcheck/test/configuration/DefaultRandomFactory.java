package com.pturpin.quickcheck.test.configuration;

import java.util.Random;

/**
 * Created by pturpin on 16/05/2017.
 */
public final class DefaultRandomFactory implements RandomFactory {

  public DefaultRandomFactory() {
    // nothing
  }

  @Override
  public Random create() {
    return new Random(0L);
  }
}
