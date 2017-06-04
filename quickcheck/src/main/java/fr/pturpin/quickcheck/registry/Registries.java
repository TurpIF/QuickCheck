package fr.pturpin.quickcheck.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import fr.pturpin.quickcheck.generator.Generator;
import fr.pturpin.quickcheck.identifier.TypeIdentifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by pturpin on 27/04/2017.
 */
public final class Registries {

  private Registries() { /* Helper class */ }

  public static RegistryBuilder builder() {
    return new RegistryBuilder();
  }

  public static Registry forMap(Map<TypeIdentifier<?>, Generator<?>> map) {
    return new MapRegistry(ImmutableMap.copyOf(Maps.transformValues(map, v -> r -> Optional.of(v))));
  }

  public static Registry empty() {
    return EmptyRegistry.EMPTY_INSTANCE;
  }

  public static Registry alternatives(Iterable<Registry> registries) {
    return new AlternativeRegistry(ImmutableList.copyOf(registries));
  }

  public static Registry alternatives(Registry... registries) {
    return new AlternativeRegistry(ImmutableList.copyOf(registries));
  }

  private static final class AlternativeRegistry implements Registry {
    private final ImmutableList<Registry> registries;

    private AlternativeRegistry(ImmutableList<Registry> registries) {
      checkArgument(registries.size() > 1);
      this.registries = registries;
    }

    @Override
    public <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier) {
      return registries.stream()
          .map(registry -> registry.lookup(identifier))
          .flatMap(Streams::stream)
          .findFirst();
    }
  }

  private static final class EmptyRegistry implements Registry {
    private static final Registry EMPTY_INSTANCE = new EmptyRegistry();

    EmptyRegistry() { /* nothing */ }

    @Override
    public <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier) {
      return Optional.empty();
    }
  }

  private static final class MapRegistry implements Registry {
    private final ImmutableMap<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> map;

    private MapRegistry(ImmutableMap<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> map) {
      this.map = checkNotNull(map);
    }

    @Override
    public <T> Optional<Generator<T>> lookup(TypeIdentifier<T> identifier) {
      Optional<Generator<?>> generator = Optional.ofNullable(map.get(identifier))
          .flatMap(f -> f.apply(this));
      return generator.map(Generator.class::cast);
    }
  }

  public static final class RegistryBuilder {
    private final ImmutableMap.Builder<TypeIdentifier<?>, Function<Registry, Optional<Generator<?>>>> builder;

    private RegistryBuilder() {
      this.builder = ImmutableMap.builder();
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, Generator<T> generator) {
      builder.put(identifier, r -> Optional.of(generator));
      return this;
    }

    public <T> RegistryBuilder put(TypeIdentifier<T> identifier, Function<Registry, Optional<Generator<T>>> generatorFactory) {
      builder.put(identifier, (Function) generatorFactory);
      return this;
    }

    public Registry build() {
      return new MapRegistry(builder.build());
    }
  }
}
