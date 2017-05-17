package com.pturpin.quickcheck.test.configuration;

import java.util.Random;

/**
 * Created by pturpin on 16/05/2017.
 */
@FunctionalInterface
public interface RandomFactory {
  Random create();
}
