package fr.pturpin.quickcheck.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import fr.pturpin.quickcheck.annotation.*;
import fr.pturpin.quickcheck.base.Optionals;
import fr.pturpin.quickcheck.base.Reflections;
import fr.pturpin.quickcheck.identifier.ClassIdentifier;
import fr.pturpin.quickcheck.identifier.ParametrizedIdentifier;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registry;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static fr.pturpin.quickcheck.base.Optionals.guard;

/**
 * Created by turpif on 27/04/17.
 */
public final class ReflectiveGenerators {

  private final Registry registry;

  private ReflectiveGenerators(Registry registry) {
    this.registry = checkNotNull(registry);
  }

  /**
   * Creates a new reflective generator factory with given generator registry.
   *
   * @param registry generator registry to use for look up
   * @return new reflective generator factory
   * @throws NullPointerException if given registry is null
   */
  public static ReflectiveGenerators with(Registry registry) {
    return new ReflectiveGenerators(registry);
  }

  /**
   * Returns a generator of objects to invoke the given method with.
   * If no register generator exists for any parameters of given method, an empty is returned.
   *
   * @see #parameterGen(Parameter)
   *
   * @param method method to lookup the generator for
   * @return generator to feed the method with
   * @throws NullPointerException if given method is null
   */
  // TODO use a Either monad to inform where the lookup had failed
  public Optional<Generator<Object[]>> parametersGen(Method method) {
    Parameter[] parameters = method.getParameters();
    Optional<List<Generator<Object>>> optGenerators = Arrays.stream(parameters)
        .map(this::parameterGen)
        .collect(Optionals.allPresent(toImmutableList()));

    return optGenerators.map(generators ->
        re -> generators.stream()
            .map(gen -> gen.get(re))
            .toArray());
  }

  /**
   * Returns a generator of objects corresponding to the given parameter.
   * If no registered generator match the given parameter, an empty is returned.
   *
   * A parameter can be decorated with several annotations that modify the fetched generators.
   * The decoration is done from right to left. So, a parameter annotated (from right to left)
   * by {@link Nullable} and then by {@link Objects.Filter} filtering null will never be null.
   *
   * @see Alias
   * @see Ints
   * @see Doubles
   * @see Longs
   * @see Objects
   * @see Nullable
   *
   * @param parameter parameter to try to find a generator for
   * @return looked up generator matching given parameter
   */
  public Optional<Generator<Object>> parameterGen(Parameter parameter) {
    Optional<Generator<Object>> optGen = registry.lookup(parameterIdentifier(parameter));

    Function<Generator<Object>, Generator<Object>> mapper = Arrays.stream(parameter.getAnnotations())
        .map(annotation -> fetchMapper(parameter, annotation))
        .flatMap(Streams::stream)
        .sequential()
        .reduce(Function.identity(), Function::compose);

    return optGen.map(mapper);
  }

  private static Optional<Function<Generator<Object>, Generator<Object>>> fetchMapper(
      Parameter parameter, Annotation annotation) {
    if (annotation instanceof Nullable) {
      double rate = ((Nullable) annotation).rate();
      return Optional.of(gen -> Generators.nullable(gen, rate));
    }

    Stream<Supplier<Optional<Function<Generator<Number>, Generator<Number>>>>> primitiveMapperFactories =
        Stream.of(
            () -> guard(isDouble(parameter), () -> (Optional) fetchDoubleMapper(annotation)),
            () -> guard(isInteger(parameter), () -> (Optional) fetchIntegerMapper(annotation)),
            () -> guard(isLong(parameter), () -> (Optional) fetchLongMapper(annotation)));

    Optional<Function<Generator<Number>, Generator<Number>>> primitiveMapper = primitiveMapperFactories
        .map(Supplier::get)
        .flatMap(Streams::stream)
        .findFirst();
    if (primitiveMapper.isPresent()) {
      return (Optional) primitiveMapper;
    }

    if (annotation instanceof Objects.Filter) {
      Class<? extends Predicate<?>> predicateKlass = ((Objects.Filter) annotation).value();
      Predicate<?> predicate = newFactory(predicateKlass);
      return Optional.of(gen -> Generators.filter(gen, (Predicate) predicate));
    }

    return Optional.empty();
  }

  private static <T> T newFactory(Class<T> klass) {
    return Reflections.uncheckedNewFactory(klass);
  }

  private static Optional<Function<Generator<Double>, Generator<Double>>> fetchDoubleMapper(Annotation annotation) {
    if (annotation instanceof Doubles.IncludeNaN) {
      double rate = ((Doubles.IncludeNaN) annotation).rate();
      return Optional.of(gen -> Generators.selection(Generators.constGen(Double.NaN), gen, Generators.coin(rate)));
    } else if (annotation instanceof Doubles.Extra) {
      Doubles.Extra doubleAnnot = (Doubles.Extra) annotation;
      double rate = doubleAnnot.rate();
      List<Double> values = Arrays.stream(doubleAnnot.values())
          .boxed()
          .collect(toImmutableList());
      return Optional.of(gen -> Generators.selection(Generators.oneOf(values), gen, Generators.coin(rate)));
    } else if (annotation instanceof Doubles.Exclude) {
      double[] excludedValues = ((Doubles.Exclude) annotation).value();
      DoublePredicate predicate = value -> Arrays.stream(excludedValues)
          .noneMatch(excluded -> Double.doubleToLongBits(excluded) == Double.doubleToLongBits(value));
      return Optional.of(gen -> Generators.filter(gen, predicate));
    } else if (annotation instanceof Doubles.Filter) {
      Class<? extends DoublePredicate> klass = ((Doubles.Filter) annotation).value();
      DoublePredicate predicate = newFactory(klass);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    } else if (annotation instanceof Doubles.Range) {
      Doubles.Range range = (Doubles.Range) annotation;
      double min = range.min();
      double max = range.max();
      boolean minIsOpen = range.minIsOpen();
      boolean maxIsOpen = range.maxIsOpen();
      checkState(max >= min);
      DoublePredicate predicate = value -> (minIsOpen ? value > min : value >= min) && (maxIsOpen ? value < max : value <= max);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    }

    return Optional.empty();
  }

  private static Optional<Function<Generator<Integer>, Generator<Integer>>> fetchIntegerMapper(Annotation annotation) {
    if (annotation instanceof Ints.Extra) {
      Ints.Extra extraAnnot = (Ints.Extra) annotation;
      double rate = extraAnnot.rate();
      List<Integer> values = Arrays.stream(extraAnnot.values())
          .boxed()
          .collect(toImmutableList());
      return Optional.of(gen -> Generators.selection(Generators.oneOf(values), gen, Generators.coin(rate)));
    } else if (annotation instanceof Ints.Exclude) {
      int[] excludedValues = ((Ints.Exclude) annotation).value();
      IntPredicate predicate = value -> Arrays.stream(excludedValues)
          .noneMatch(excluded -> excluded == value);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    } else if (annotation instanceof Ints.Filter) {
      Class<? extends IntPredicate> klass = ((Ints.Filter) annotation).value();
      IntPredicate predicate = newFactory(klass);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    } else if (annotation instanceof Ints.Range) {
      Ints.Range range = (Ints.Range) annotation;
      int min = range.min();
      int max = range.max();
      boolean minIsOpen = range.minIsOpen();
      boolean maxIsOpen = range.maxIsOpen();
      checkState(max >= min);
      IntPredicate predicate = value -> (minIsOpen ? value > min : value >= min) && (maxIsOpen ? value < max : value <= max);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    }

    return Optional.empty();
  }

  private static Optional<Function<Generator<Long>, Generator<Long>>> fetchLongMapper(Annotation annotation) {
    if (annotation instanceof Longs.Extra) {
      Longs.Extra extraAnnot = (Longs.Extra) annotation;
      double rate = extraAnnot.rate();
      List<Long> values = Arrays.stream(extraAnnot.values())
          .boxed()
          .collect(toImmutableList());
      return Optional.of(gen -> Generators.selection(Generators.oneOf(values), gen, Generators.coin(rate)));
    } else if (annotation instanceof Longs.Exclude) {
      long[] excludedValues = ((Longs.Exclude) annotation).value();
      LongPredicate predicate = value -> Arrays.stream(excludedValues)
          .noneMatch(excluded -> excluded == value);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    } else if (annotation instanceof Longs.Filter) {
      Class<? extends LongPredicate> klass = ((Longs.Filter) annotation).value();
      LongPredicate predicate = newFactory(klass);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    } else if (annotation instanceof Longs.Range) {
      Longs.Range range = (Longs.Range) annotation;
      long min = range.min();
      long max = range.max();
      boolean minIsOpen = range.minIsOpen();
      boolean maxIsOpen = range.maxIsOpen();
      checkState(max >= min);
      LongPredicate predicate = value -> (minIsOpen ? value > min : value >= min) && (maxIsOpen ? value < max : value <= max);
      return Optional.of(gen -> Generators.filter(gen, predicate));
    }

    return Optional.empty();
  }

  private static boolean isDouble(Parameter parameter) {
    return Double.class.isAssignableFrom(parameter.getType())
        || double.class.isAssignableFrom(parameter.getType());
  }

  private static boolean isInteger(Parameter parameter) {
    return Integer.class.isAssignableFrom(parameter.getType())
        || int.class.isAssignableFrom(parameter.getType());
  }

  private static boolean isLong(Parameter parameter) {
    return Long.class.isAssignableFrom(parameter.getType())
        || long.class.isAssignableFrom(parameter.getType());
  }

  private static TypeIdentifier<Object> parameterIdentifier(Parameter parameter) {
    Alias aliasAnnot = parameter.getAnnotation(Alias.class);
    if (aliasAnnot != null) {
      Class<?> aliasType = aliasAnnot.value();
      return new ClassIdentifier<>((Class) aliasType);
    }
    return getParametrizedType(parameter.getParameterizedType());
  }

  private static TypeIdentifier<Object> getParametrizedType(Type type) {
    if (type instanceof Class<?>) {
      return new ClassIdentifier<>((Class) type);
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      TypeIdentifier<Object> rawIdentifier = getParametrizedType(parameterizedType.getRawType());
      List<TypeIdentifier<?>> paramIdentifiers = Arrays.stream(parameterizedType.getActualTypeArguments())
          .map(ReflectiveGenerators::getParametrizedType)
          .collect(toImmutableList());
      return ParametrizedIdentifier.paramId(rawIdentifier, paramIdentifiers);
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      Type[] upperBounds = wildcardType.getUpperBounds();
      Preconditions.checkState(upperBounds.length == 1, "Wildcard type are handled only with single upper bound: %s", type);
      return getParametrizedType(upperBounds[0]);
    }
    throw new UnsupportedOperationException("Not supported type: " + type);
  }
}
