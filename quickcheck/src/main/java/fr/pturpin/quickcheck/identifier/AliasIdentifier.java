package fr.pturpin.quickcheck.identifier;

import static com.google.common.base.Preconditions.checkNotNull;

public class AliasIdentifier<T> implements TypeIdentifier<T> {
  private final TypeIdentifier<T> identifier;

  public AliasIdentifier(TypeIdentifier<T> identifier) {
    this.identifier = checkNotNull(identifier);
  }

  @Override
  public Class<T> getTypeClass() {
    return identifier.getTypeClass();
  }

  @Override
  public boolean equals(Object obj) {
    // Explicit equality by ref
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // Explicit native hash code
    return super.hashCode();
  }
}
