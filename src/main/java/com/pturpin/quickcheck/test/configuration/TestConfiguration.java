package com.pturpin.quickcheck.test.configuration;

import com.pturpin.quickcheck.registry.Registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.pturpin.quickcheck.test.configuration.TestRunnerConfigurations.DEFAULT_ACCEPT_SKIPPED;

/**
 * Created by pturpin on 16/05/2017.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestConfiguration {

  int nbRun() default NONE_NB_RUN;
  boolean acceptSkipped() default DEFAULT_ACCEPT_SKIPPED;
  Class<? extends RandomFactory> random() default NoneRandomFactory.class;
  Class<? extends RegistryFactory> registry() default NoneRegistryFactory.class;

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface NbRun {
    int value();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Skipped {
    boolean accepted();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Random {
    Class<? extends RandomFactory> value();
  }

  int NONE_NB_RUN = -1;

  final class NoneRandomFactory implements RandomFactory {
    @Override
    public java.util.Random create() {
      throw new UnsupportedOperationException();
    }
  }

  final class NoneRegistryFactory implements RegistryFactory {
    @Override
    public Registry create() {
      throw new UnsupportedOperationException();
    }
  }
}
