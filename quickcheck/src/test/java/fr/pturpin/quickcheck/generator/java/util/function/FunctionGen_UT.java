package fr.pturpin.quickcheck.generator.java.util.function;

import com.google.common.collect.ImmutableMap;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.generator.NumberGens;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;
import fr.pturpin.quickcheck.test.configuration.DefaultRegistryFactory;
import org.junit.Test;

import java.util.function.*;
import java.util.stream.Stream;

import static fr.pturpin.quickcheck.generator.RegistryAssertions.assertIsInRegistry;
import static fr.pturpin.quickcheck.generator.RegistryAssertions.getIdentifier;
import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

/**
 * Created by pturpin on 17/06/2017.
 */
public class FunctionGen_UT {

  private static final TypeIdentifier<?> PARAM_FILLER = classId(double.class);

  @Test
  public void supplierShouldBeInDefaultRegistry() {
    assertIsInRegistries(Supplier.class);
  }

  @Test
  public void doubleSupplierShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoubleSupplier.class);
  }

  @Test
  public void longSupplierShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongSupplier.class);
  }

  @Test
  public void intSupplierShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntSupplier.class);
  }

  @Test
  public void booleanSupplierShouldBeInDefaultRegistry() {
    assertIsInRegistries(BooleanSupplier.class);
  }

  @Test
  public void unaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(UnaryOperator.class);
  }

  @Test
  public void binaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(BinaryOperator.class);
  }

  @Test
  public void doubleUnaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoubleUnaryOperator.class);
  }

  @Test
  public void doubleBinaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoubleBinaryOperator.class);
  }

  @Test
  public void longUnaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongUnaryOperator.class);
  }

  @Test
  public void longBinaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongBinaryOperator.class);
  }

  @Test
  public void intUnaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntUnaryOperator.class);
  }

  @Test
  public void intBinaryOperatorShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntBinaryOperator.class);
  }

  @Test
  public void predicateShouldBeInDefaultRegistry() {
    assertIsInRegistries(Predicate.class);
  }

  @Test
  public void biPredicateShouldBeInDefaultRegistry() {
    assertIsInRegistries(BiPredicate.class);
  }

  @Test
  public void doublePredicateShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoublePredicate.class);
  }

  @Test
  public void longPredicateShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongPredicate.class);
  }

  @Test
  public void intPredicateShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntPredicate.class);
  }

  @Test
  public void functionShouldBeInDefaultRegistry() {
    assertIsInRegistries(Function.class);
  }

  @Test
  public void doubleFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoubleFunction.class);
  }

  @Test
  public void longFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongFunction.class);
  }

  @Test
  public void intFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntFunction.class);
  }

  @Test
  public void toDoubleFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(ToDoubleFunction.class);
  }

  @Test
  public void LongToDoubleFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongToDoubleFunction.class);
  }

  @Test
  public void IntToDoubleFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntToDoubleFunction.class);
  }

  @Test
  public void toLongFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(ToLongFunction.class);
  }

  @Test
  public void DoubleToLongFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoubleToLongFunction.class);
  }

  @Test
  public void IntToLongFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(IntToLongFunction.class);
  }

  @Test
  public void toIntFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(ToIntFunction.class);
  }

  @Test
  public void DoubleToIntFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(DoubleToIntFunction.class);
  }

  @Test
  public void LongToIntFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(LongToIntFunction.class);
  }

  @Test
  public void biFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(BiFunction.class);
  }

  @Test
  public void toDoubleBiFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(ToDoubleBiFunction.class);
  }

  @Test
  public void toLongBiFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(ToLongBiFunction.class);
  }

  @Test
  public void toIntBiFunctionShouldBeInDefaultRegistry() {
    assertIsInRegistries(ToIntBiFunction.class);
  }

  private static Stream<Registry> getRegistries() {
    return Stream.of(new DefaultRegistryFactory().create(),
        Registries.alternatives(FunctionGen.functionsRegistry(),
            Registries.forMap(ImmutableMap.<TypeIdentifier<?>, Generator<?>>builder()
                .put(classId(long.class), NumberGens.longGen())
                .put(classId(int.class), NumberGens.integerGen())
                .put(classId(double.class), NumberGens.doubleGen())
                .put(classId(boolean.class), Generators.coin(0.5))
                .build())));
  }

  private static <T> void assertIsInRegistries(Class<T> klass) {
    getRegistries().forEach(registry -> assertIsInRegistry(registry, klass, k -> getIdentifier(k, PARAM_FILLER)));
  }

}
