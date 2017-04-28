package com.pturpin.quickcheck.generator;

import com.google.common.base.Preconditions;
import com.pturpin.quickcheck.base.Optionals;
import com.pturpin.quickcheck.identifier.ClassIdentifier;
import com.pturpin.quickcheck.identifier.TypeIdentifier;
import com.pturpin.quickcheck.registry.Registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

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
    return registry.lookup(parameterIdentifier(parameter));
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
