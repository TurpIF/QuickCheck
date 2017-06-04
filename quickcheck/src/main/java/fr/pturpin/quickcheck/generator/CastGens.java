package fr.pturpin.quickcheck.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.primitives.Primitives.*;

/**
 * Created by pturpin on 04/06/2017.
 */
public final class CastGens {

  private CastGens() {
    // Nothing
  }

  /**
   * Casted generator to given target class.
   *
   * @param generator base generator
   * @param targetKlass class to map the given generator
   * @param <T> type of initial generator's elements
   * @param <R> type of casted generator's elements
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static <T, R> Generator<R> cast(Generator<? extends T> generator, Class<? extends R> targetKlass) {
    checkNotNull(targetKlass);
    return Generators.map(generator, targetKlass::cast);
  }

  /**
   * Casted generator from int to long
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   *
   * @param generator base generator
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static Generator<Long> intToLong(Generator<Integer> generator) {
    return primitiveCast(generator, int.class, long.class);
  }

  /**
   * Casted generator from long to int
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   * So yielded element may lose precisions
   *
   * @param generator base generator
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static Generator<Integer> longToInt(Generator<Long> generator) {
    return primitiveCast(generator, long.class, int.class);
  }

  /**
   * Casted generator from double to int
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   * So yielded element may lose precisions
   *
   * @param generator base generator
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static Generator<Integer> doubleToInt(Generator<Double> generator) {
    return primitiveCast(generator, double.class, int.class);
  }

  /**
   * Casted generator from double to long
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   * So yielded element may lose precisions
   *
   * @param generator base generator
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static Generator<Long> doubleToLong(Generator<Double> generator) {
    return primitiveCast(generator, double.class, long.class);
  }

  /**
   * Casted generator from int to double
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   *
   * @param generator base generator
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static Generator<Double> intToDouble(Generator<Integer> generator) {
    return primitiveCast(generator, int.class, double.class);
  }

  /**
   * Casted generator from long to double
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   *
   * @param generator base generator
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   */
  public static Generator<Double> longToDouble(Generator<Long> generator) {
    return primitiveCast(generator, long.class, double.class);
  }

  /**
   * Casted primitive generator to given target class.
   *
   * The primitive cast follow the <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2">JLS rules</a>.
   * So casted generator may yield truncated elements. Also, the boolean primitive type is not accepted.
   *
   * @param generator base generator
   * @param currentKlass current primitive or boxed class of the generator
   * @param targetKlass primitive or boxed class to map the given generator
   * @param <T> type of initial generator's elements
   * @param <R> type of casted generator's elements
   * @return casted generator
   * @throws NullPointerException if given generator or target class are null
   * @throws IllegalArgumentException if given classes are not primitives (nor boxed versions) or if any is {@link Boolean}
   */
  public static <T, R> Generator<R> primitiveCast(Generator<T> generator, Class<T> currentKlass, Class<R> targetKlass) {
    checkArgument(isWrapperType(wrap(currentKlass)));
    checkArgument(isWrapperType(wrap(targetKlass)));

    Class<?> currentUnboxedKlass = unwrap(currentKlass);
    Class<?> targetUnboxedKlass = unwrap(targetKlass);

    checkArgument(currentUnboxedKlass != boolean.class);
    checkArgument(targetUnboxedKlass != boolean.class);

    if (int.class.equals(currentUnboxedKlass)) {
      Generator<Integer> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        Generator<Long> targetGen = Generators.map(baseGen, Integer::longValue);
        return (Generator) targetGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        Generator<Short> targetGen = Generators.map(baseGen, Integer::shortValue);
        return (Generator) targetGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        Generator<Float> targetGen = Generators.map(baseGen, Integer::floatValue);
        return (Generator) targetGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        Generator<Double> targetGen = Generators.map(baseGen, Integer::doubleValue);
        return (Generator) targetGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        Generator<Byte> targetGen = Generators.map(baseGen, Integer::byteValue);
        return (Generator) targetGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        Generator<Character> targetGen = Generators.map(baseGen, v -> (char) v.intValue());
        return (Generator) targetGen;
      }
    } else if (long.class.equals(currentUnboxedKlass)) {
      Generator<Long> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        Generator<Integer> targetGen = Generators.map(baseGen, Long::intValue);
        return (Generator) targetGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        Generator<Short> targetGen = Generators.map(baseGen, Long::shortValue);
        return (Generator) targetGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        Generator<Float> targetGen = Generators.map(baseGen, Long::floatValue);
        return (Generator) targetGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        Generator<Double> targetGen = Generators.map(baseGen, Long::doubleValue);
        return (Generator) targetGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        Generator<Byte> targetGen = Generators.map(baseGen, Long::byteValue);
        return (Generator) targetGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        Generator<Character> targetGen = Generators.map(baseGen, v -> (char) v.longValue());
        return (Generator) targetGen;
      }
    } else if (short.class.equals(currentUnboxedKlass)) {
      Generator<Short> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        Generator<Integer> targetGen = Generators.map(baseGen, Short::intValue);
        return (Generator) targetGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        Generator<Long> targetGen = Generators.map(baseGen, Short::longValue);
        return (Generator) targetGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        Generator<Float> targetGen = Generators.map(baseGen, Short::floatValue);
        return (Generator) targetGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        Generator<Double> targetGen = Generators.map(baseGen, Short::doubleValue);
        return (Generator) targetGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        Generator<Byte> targetGen = Generators.map(baseGen, Short::byteValue);
        return (Generator) targetGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        Generator<Character> targetGen = Generators.map(baseGen, v -> (char) v.shortValue());
        return (Generator) targetGen;
      }
    } else if (float.class.equals(currentUnboxedKlass)) {
      Generator<Float> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        Generator<Integer> targetGen = Generators.map(baseGen, Float::intValue);
        return (Generator) targetGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        Generator<Long> targetGen = Generators.map(baseGen, Float::longValue);
        return (Generator) targetGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        Generator<Short> targetGen = Generators.map(baseGen, Float::shortValue);
        return (Generator) targetGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        Generator<Double> targetGen = Generators.map(baseGen, Float::doubleValue);
        return (Generator) targetGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        Generator<Byte> targetGen = Generators.map(baseGen, Float::byteValue);
        return (Generator) targetGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        Generator<Character> targetGen = Generators.map(baseGen, v -> (char) v.floatValue());
        return (Generator) targetGen;
      }
    } else if (double.class.equals(currentUnboxedKlass)) {
      Generator<Double> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        Generator<Integer> targetGen = Generators.map(baseGen, Double::intValue);
        return (Generator) targetGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        Generator<Long> targetGen = Generators.map(baseGen, Double::longValue);
        return (Generator) targetGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        Generator<Short> targetGen = Generators.map(baseGen, Double::shortValue);
        return (Generator) targetGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        Generator<Float> targetGen = Generators.map(baseGen, Double::floatValue);
        return (Generator) targetGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        Generator<Byte> targetGen = Generators.map(baseGen, Double::byteValue);
        return (Generator) targetGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        Generator<Character> targetGen = Generators.map(baseGen, v -> (char) v.doubleValue());
        return (Generator) targetGen;
      }
    } else if (byte.class.equals(currentUnboxedKlass)) {
      Generator<Byte> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        Generator<Integer> targetGen = Generators.map(baseGen, Byte::intValue);
        return (Generator) targetGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        Generator<Long> targetGen = Generators.map(baseGen, Byte::longValue);
        return (Generator) targetGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        Generator<Short> targetGen = Generators.map(baseGen, Byte::shortValue);
        return (Generator) targetGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        Generator<Float> targetGen = Generators.map(baseGen, Byte::floatValue);
        return (Generator) targetGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        Generator<Double> targetGen = Generators.map(baseGen, Byte::doubleValue);
        return (Generator) targetGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        Generator<Character> targetGen = Generators.map(baseGen, v -> (char) v.byteValue());
        return (Generator) targetGen;
      }
    } else if (char.class.equals(currentUnboxedKlass)) {
      Generator<Character> baseGen = (Generator) generator;
      if (int.class.equals(targetUnboxedKlass)) {
        Generator<Integer> targetGen = Generators.map(baseGen, v -> (int) v);
        return (Generator) targetGen;
      } else if (long.class.equals(targetUnboxedKlass)) {
        Generator<Long> targetGen = Generators.map(baseGen, v -> (long) v);
        return (Generator) targetGen;
      } else if (short.class.equals(targetUnboxedKlass)) {
        Generator<Short> targetGen = Generators.map(baseGen, v -> (short) v.charValue());
        return (Generator) targetGen;
      } else if (float.class.equals(targetUnboxedKlass)) {
        Generator<Float> targetGen = Generators.map(baseGen, v -> (float) v);
        return (Generator) targetGen;
      } else if (double.class.equals(targetUnboxedKlass)) {
        Generator<Double> targetGen = Generators.map(baseGen, v -> (double) v);
        return (Generator) targetGen;
      } else if (byte.class.equals(targetUnboxedKlass)) {
        Generator<Byte> targetGen = Generators.map(baseGen, v -> (byte) v.charValue());
        return (Generator) targetGen;
      } else if (char.class.equals(targetUnboxedKlass)) {
        return (Generator) baseGen;
      }
    }
    throw new IllegalArgumentException("Impossible to cast " + currentKlass + " to " + targetKlass);
  }

}
