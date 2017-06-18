package fr.pturpin.quickcheck.test.configuration;

import fr.pturpin.quickcheck.base.Ranges;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.generator.java.math.JavaMaths;
import fr.pturpin.quickcheck.generator.java.util.JavaUtils;
import fr.pturpin.quickcheck.generator.java.util.function.FunctionGen;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

/**
 * Created by turpif on 28/04/17.
 */
public final class DefaultRegistryFactory implements RegistryFactory {

  public DefaultRegistryFactory() {
    // nothing
  }

  @Override
  public Registry create() {
    Registry base = Registries.builder()
        .put(classId(double.class), NumberGens.doubleGen())
        .put(classId(int.class), NumberGens.integerGen(Ranges.closed(0, 50))) // FIXME handle collection size correctly
        .put(classId(long.class), NumberGens.longGen())
        .put(classId(boolean.class), Generators.coin(0.5))
        .build();

    return Registries.alternatives(
        base,
        JavaUtils.utilsRegistry(),
        JavaMaths.mathRegistry(),
        FunctionGen.functionsRegistry());
  }

}
