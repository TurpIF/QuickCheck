package com.pturpin.quickcheck.generator;

import com.google.common.collect.Streams;
import com.pturpin.quickcheck.annotation.Doubles;
import com.pturpin.quickcheck.annotation.Nullable;
import com.pturpin.quickcheck.base.Optionals;
import com.pturpin.quickcheck.base.Reflections;
import com.pturpin.quickcheck.identifier.ClassIdentifier;
import com.pturpin.quickcheck.identifier.TypeIdentifier;
import com.pturpin.quickcheck.registry.Registry;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.DoublePredicate;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.pturpin.quickcheck.generator.Generators.*;

/**
 * Created by turpif on 27/04/17.
 */
public final class ReflectiveGenerators {

  private final Registry registry;

  private ReflectiveGenerators(Registry registry) {
    this.registry = checkNotNull(registry);
  }

  public static ReflectiveGenerators with(Registry registry) {
    return new ReflectiveGenerators(registry);
  }

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

  public Optional<Generator<Object>> parameterGen(Parameter parameter) {
    Optional<Generator<Object>> optGen = registry.lookup(parameterIdentifier(parameter));

    Function<Generator<Object>, Generator<Object>> mapper = Arrays.stream(parameter.getAnnotations())
        .map(annot -> fetchMapper(parameter, annot))
        .flatMap(Streams::stream)
        .sequential()
        .reduce(Function.identity(), Function::compose);

    return optGen.map(mapper);
  }

  private static Optional<Function<Generator<Object>, Generator<Object>>> fetchMapper(
      Parameter parameter, Annotation annot) {
    if (annot instanceof Nullable) {
      double rate = ((Nullable) annot).rate();
      return Optional.of(gen -> Generators.nullable(gen, rate));
    } else if (annot instanceof Doubles.IncludeNaN) {
      checkState(isDouble(parameter));
      double rate = ((Doubles.IncludeNaN) annot).rate();
      return Optional.of(gen -> selection(constGen(Double.NaN), gen, coin(rate)));
    } else if (annot instanceof Doubles.Extra) {
      checkState(isDouble(parameter));
      Doubles.Extra doubleAnnot = (Doubles.Extra) annot;
      double rate = doubleAnnot.rate();
      List<Double> values = Arrays.stream(doubleAnnot.values())
          .boxed()
          .collect(toImmutableList());
      return Optional.of(gen -> selection(oneOf(values), gen, coin(rate)));
    } else if (annot instanceof Doubles.Exclude) {
      checkState(isDouble(parameter));
      double[] values = ((Doubles.Exclude) annot).value();
      return Optional.of(gen -> filter(gen, v -> Arrays.stream(values)
          .noneMatch(value -> !v.equals(value))));
    } else if (annot instanceof Doubles.Filter) {
      checkState(isDouble(parameter));
      Class<? extends DoublePredicate> klass = ((Doubles.Filter) annot).value();
      DoublePredicate predicate = Reflections.uncheckedNewFactory(klass);
      return Optional.of(gen -> (Generator) Generators.filter((Generator) gen, predicate));
    } else if (annot instanceof Doubles.OpenRange) {
      throw new UnsupportedOperationException(); // TODO
    }

    // TODO ints, longs, objects

    return Optional.empty();
  }

  private static boolean isDouble(Parameter parameter) {
    return Double.class.isAssignableFrom(parameter.getType())
           || double.class.isAssignableFrom(parameter.getType());
  }

  private static TypeIdentifier<Object> parameterIdentifier(Parameter parameter) {
    Alias aliasAnnot = parameter.getAnnotation(Alias.class);
    if (aliasAnnot != null) {
      Class<?> aliasType = aliasAnnot.value();
      return new ClassIdentifier<>((Class) aliasType);
    }

    Class<?> paramType = parameter.getType();
    return new ClassIdentifier<>((Class) paramType);
  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Alias {
    Class<?> value();
  }
}
