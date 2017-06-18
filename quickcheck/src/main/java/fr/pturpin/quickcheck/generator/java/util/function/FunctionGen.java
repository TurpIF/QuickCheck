package fr.pturpin.quickcheck.generator.java.util.function;

import fr.pturpin.quickcheck.annotation.Gen;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.generator.Generators;
import fr.pturpin.quickcheck.registry.Registries;
import fr.pturpin.quickcheck.registry.Registry;

import java.util.function.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by pturpin on 17/06/2017.
 */
public final class FunctionGen {
  private FunctionGen() {
  }

  /**
   * Returns a supplier generator from an output generator.
   *
   * @param generator output generator
   * @param <T> type of supplied elements
   * @return supplier generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<Supplier<T>> supplierGen(Generator<T> generator) {
    return Generators.map(generator, v -> () -> v);
  }

  /**
   * Returns a double supplier generator from an output generator.
   *
   * @param generator output generator
   * @return supplier generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<DoubleSupplier> doubleSupplierGen(Generator<Double> generator) {
    return Generators.map(generator, v -> () -> v);
  }

  /**
   * Returns a long supplier generator from an output generator.
   *
   * @param generator output generator
   * @return supplier generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<LongSupplier> longSupplierGen(Generator<Long> generator) {
    return Generators.map(generator, v -> () -> v);
  }

  /**
   * Returns a integer supplier generator from an output generator.
   *
   * @param generator output generator
   * @return supplier generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<IntSupplier> intSupplierGen(Generator<Integer> generator) {
    return Generators.map(generator, v -> () -> v);
  }

  /**
   * Returns a boolean supplier generator from an output generator.
   *
   * @param generator output generator
   * @return supplier generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<BooleanSupplier> booleanSupplierGen(Generator<Boolean> generator) {
    return Generators.map(generator, v -> () -> v);
  }

  /**
   * Returns an unary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of operand and result elements
   * @return unary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<UnaryOperator<T>> unaryOperatorGen(Generator<T> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a double unary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return unary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<DoubleUnaryOperator> doubleUnaryOperatorGen(Generator<Double> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a long unary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return unary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<LongUnaryOperator> longUnaryOperatorGen(Generator<Long> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns an integer unary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return unary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<IntUnaryOperator> intUnaryOperatorGen(Generator<Integer> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a binary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of operands and result elements
   * @return binary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<BinaryOperator<T>> binaryOperatorGen(Generator<T> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<T> leftGen = Generators.coGenerator(left, generator);
      Generator<T> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a double binary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return binary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<DoubleBinaryOperator> doubleBinaryOperatorGen(Generator<Double> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Double> leftGen = Generators.coGenerator(left, generator);
      Generator<Double> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a long binary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return binary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<LongBinaryOperator> longBinaryOperatorGen(Generator<Long> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Long> leftGen = Generators.coGenerator(left, generator);
      Generator<Long> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a integer binary operator generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return binary operator generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<IntBinaryOperator> intBinaryOperatorGen(Generator<Integer> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Integer> leftGen = Generators.coGenerator(left, generator);
      Generator<Integer> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a predicate generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the input of predicate
   * @return predicate generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<Predicate<T>> predicateGen(Generator<Boolean> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a double predicate generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return predicate generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<DoublePredicate> doublePredicateGen(Generator<Boolean> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a long predicate generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return predicate generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<LongPredicate> longPredicateGen(Generator<Boolean> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a integer predicate generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return predicate generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<IntPredicate> intPredicateGen(Generator<Boolean> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a binary predicate generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the first input of the predicate
   * @param <U> type of the second input of the predicate
   * @return binary predicate generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T, U> Generator<BiPredicate<T, U>> biPredicateGen(Generator<Boolean> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Boolean> leftGen = Generators.coGenerator(left, generator);
      Generator<Boolean> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a function generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the input of the function
   * @param <R> type of the output of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T, R> Generator<Function<T, R>> functionGen(Generator<R> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a double function generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <R> type of the output of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <R> Generator<DoubleFunction<R>> doubleFunctionGen(Generator<R> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a long function generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <R> type of the output of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <R> Generator<LongFunction<R>> longFunctionGen(Generator<R> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a integer function generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <R> type of the output of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <R> Generator<IntFunction<R>> intFunctionGen(Generator<R> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function to double generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the input of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<ToDoubleFunction<T>> toDoubleFunctionGen(Generator<Double> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function from long to double generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<LongToDoubleFunction> longToDoubleFunctionGen(Generator<Double> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function from integer to double generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<IntToDoubleFunction> intToDoubleFunctionGen(Generator<Double> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function to long generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the input of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<ToLongFunction<T>> toLongFunctionGen(Generator<Long> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function from double to long generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<DoubleToLongFunction> doubleToLongFunctionGen(Generator<Long> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function from integer to long generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<IntToLongFunction> intToLongFunctionGen(Generator<Long> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function to integer generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the input of the function
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T> Generator<ToIntFunction<T>> toIntFunctionGen(Generator<Integer> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function from double to integer generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<DoubleToIntFunction> doubleToIntFunctionGen(Generator<Integer> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a function from long to integer generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @return function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static Generator<LongToIntFunction> longToIntFunctionGen(Generator<Integer> generator) {
    checkNotNull(generator);
    return re -> value -> Generators.coGenerator(value, generator).get(re);
  }

  /**
   * Returns a binary function generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the first input of the bi-function
   * @param <U> type of the second input of the bi-function
   * @param <R> type of the output of the bi-function
   * @return binary function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T, U, R> Generator<BiFunction<T, U, R>> biFunctionGen(Generator<R> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<R> leftGen = Generators.coGenerator(left, generator);
      Generator<R> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a binary function to double generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the first input of the bi-function
   * @param <U> type of the second input of the bi-function
   * @return binary function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T, U> Generator<ToDoubleBiFunction<T, U>> toDoubleBiFunctionGen(Generator<Double> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Double> leftGen = Generators.coGenerator(left, generator);
      Generator<Double> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a binary function to long generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the first input of the bi-function
   * @param <U> type of the second input of the bi-function
   * @return binary function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T, U> Generator<ToLongBiFunction<T, U>> toLongBiFunctionGen(Generator<Long> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Long> leftGen = Generators.coGenerator(left, generator);
      Generator<Long> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a binary function to integer generator from an output generator.
   * It's guarantee that the operators always yield the same output given the same input.
   *
   * @param generator output generator
   * @param <T> type of the first input of the bi-function
   * @param <U> type of the second input of the bi-function
   * @return binary function generator
   * @throws NullPointerException if given generator is null
   */
  @Gen
  public static <T, U> Generator<ToIntBiFunction<T, U>> toIntBiFunctionGen(Generator<Integer> generator) {
    checkNotNull(generator);
    return re -> (left, right) -> {
      Generator<Integer> leftGen = Generators.coGenerator(left, generator);
      Generator<Integer> rightGen = Generators.coGenerator(right, leftGen);
      return rightGen.get(re);
    };
  }

  /**
   * Returns a registry containing all functional generator of this class for interfaces in {@link java.util.function}.
   * This registry is not self sufficient as it need output generators
   * such as Double, Long, Integer, Boolean and other Object generators.
   * <p>
   * The contained generators are :<br>
   * <ul>
   *   <li>{@link Supplier}: {@link #supplierGen(Generator)}</li>
   *   <li>{@link DoubleSupplier}: {@link #doubleSupplierGen(Generator)}</li>
   *   <li>{@link LongSupplier}: {@link #longSupplierGen(Generator)}</li>
   *   <li>{@link IntSupplier}: {@link #intSupplierGen(Generator)}</li>
   *   <li>{@link BooleanSupplier}: {@link #booleanSupplierGen(Generator)}</li>
   *   <li>{@link UnaryOperator}: {@link #unaryOperatorGen(Generator)}</li>
   *   <li>{@link DoubleUnaryOperator}: {@link #doubleUnaryOperatorGen(Generator)}</li>
   *   <li>{@link LongUnaryOperator}: {@link #longUnaryOperatorGen(Generator)}</li>
   *   <li>{@link IntUnaryOperator}: {@link #intUnaryOperatorGen(Generator)}</li>
   *   <li>{@link BinaryOperator}: {@link #binaryOperatorGen(Generator)}</li>
   *   <li>{@link DoubleBinaryOperator}: {@link #doubleBinaryOperatorGen(Generator)}</li>
   *   <li>{@link LongBinaryOperator}: {@link #longBinaryOperatorGen(Generator)}</li>
   *   <li>{@link IntBinaryOperator}: {@link #intBinaryOperatorGen(Generator)}</li>
   *   <li>{@link Predicate}: {@link #predicateGen(Generator)}</li>
   *   <li>{@link DoublePredicate}: {@link #doublePredicateGen(Generator)}</li>
   *   <li>{@link LongPredicate}: {@link #longPredicateGen(Generator)}</li>
   *   <li>{@link IntPredicate}: {@link #intPredicateGen(Generator)}</li>
   *   <li>{@link BiPredicate}: {@link #biPredicateGen(Generator)}</li>
   *   <li>{@link Function}: {@link #functionGen(Generator)}</li>
   *   <li>{@link DoubleFunction}: {@link #doubleFunctionGen(Generator)}</li>
   *   <li>{@link LongFunction}: {@link #longFunctionGen(Generator)}</li>
   *   <li>{@link IntFunction}: {@link #intFunctionGen(Generator)}</li>
   *   <li>{@link ToDoubleFunction}: {@link #toDoubleFunctionGen(Generator)}</li>
   *   <li>{@link LongToDoubleFunction}: {@link #longToDoubleFunctionGen(Generator)}</li>
   *   <li>{@link IntToDoubleFunction}: {@link #intToDoubleFunctionGen(Generator)}</li>
   *   <li>{@link ToLongFunction}: {@link #toLongFunctionGen(Generator)}</li>
   *   <li>{@link DoubleToLongFunction}: {@link #doubleToLongFunctionGen(Generator)}</li>
   *   <li>{@link IntToLongFunction}: {@link #intToLongFunctionGen(Generator)}</li>
   *   <li>{@link ToIntFunction}: {@link #toIntFunctionGen(Generator)}</li>
   *   <li>{@link DoubleToIntFunction}: {@link #doubleToIntFunctionGen(Generator)}</li>
   *   <li>{@link LongToIntFunction}: {@link #longToIntFunctionGen(Generator)}</li>
   *   <li>{@link BiFunction}: {@link #biFunctionGen(Generator)}</li>
   *   <li>{@link ToDoubleBiFunction}: {@link #toDoubleBiFunctionGen(Generator)}</li>
   *   <li>{@link ToLongBiFunction}: {@link #toLongBiFunctionGen(Generator)}</li>
   *   <li>{@link ToIntBiFunction}: {@link #toIntBiFunctionGen(Generator)}</li>
   * </ul>
   *
   * @return registry of function interface of {@link java.util.function}
   */
  public static Registry functionsRegistry() {
    return Registries.forClass(FunctionGen.class);
  }
}
