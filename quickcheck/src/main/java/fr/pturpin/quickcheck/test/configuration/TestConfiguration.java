package fr.pturpin.quickcheck.test.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static fr.pturpin.quickcheck.test.configuration.TestRunnerConfigurations.DEFAULT_ACCEPT_SKIPPED;

/**
 * Created by pturpin on 16/05/2017.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestConfiguration {

  long nbRun() default NONE_NB_RUN;
  double acceptSkipped() default DEFAULT_ACCEPT_SKIPPED;
  Class<? extends RandomFactory> random() default NoneRandomFactory.class;
  Class<? extends RegistryFactory> registry() default NoneRegistryFactory.class;

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface NbRun {
    long value();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Skipped {
    double value();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Random {
    Class<? extends RandomFactory> value();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Registry {
    Class<? extends RegistryFactory> value();
  }

  long NONE_NB_RUN = -1;

  final class NoneRandomFactory implements RandomFactory {
    @Override
    public java.util.Random create() {
      throw new UnsupportedOperationException();
    }
  }

  final class NoneRegistryFactory implements RegistryFactory {
    @Override
    public fr.pturpin.quickcheck.registry.Registry create() {
      throw new UnsupportedOperationException();
    }
  }
}
