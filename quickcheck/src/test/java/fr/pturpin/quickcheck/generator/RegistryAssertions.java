package fr.pturpin.quickcheck.generator;

import com.google.common.collect.ImmutableList;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;
import fr.pturpin.quickcheck.registry.Registry;
import org.junit.Assert;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fr.pturpin.quickcheck.identifier.Identifiers.classId;
import static fr.pturpin.quickcheck.identifier.Identifiers.paramId;

/**
 * Created by pturpin on 18/06/2017.
 */
public class RegistryAssertions {

  public static <T> void assertIsInRegistry(Registry registry, Class<T> klass, Function<Class<T>, TypeIdentifier<T>> identifierF) {
    TypeIdentifier<T> identifier = identifierF.apply(klass);
    Optional<? extends Generator<T>> generator = registry.lookup(identifier);
    Assert.assertTrue(generator.isPresent());

    Random re = new Random(0);
    for (int i = 0; i < 1000; i++) {
      T object = generator.get().get(re);
      Assert.assertNotNull(object);
      Assert.assertTrue(klass.isAssignableFrom(object.getClass()));
    }
  }

  public static <T> TypeIdentifier<T> getIdentifier(Class<T> klass, TypeIdentifier<?> filler) {
    TypeIdentifier<T> classId = classId(klass);
    if (!classId.getParametrizedType().isPresent()) {
      ImmutableList<TypeIdentifier<?>> parameters = Stream.generate(() -> filler)
          .limit(classId.getNbParametrizedType())
          .collect(toImmutableList());
      return paramId(classId, parameters);
    }
    return classId;
  }

}
