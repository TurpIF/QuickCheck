package com.pturpin.quickcheck.identifier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ClassIdentifier<T> implements TypeIdentifier<T> {
    private final Class<T> klass;

    public ClassIdentifier(Class<T> klass) {
        this.klass = checkNotNull(klass);
    }

    @Override
    public Class<T> getTypeClass() {
        return klass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != ClassIdentifier.class) {
            return false;
        }
        ClassIdentifier other = (ClassIdentifier) obj;
        return klass.equals(other.klass);
    }

    @Override
    public int hashCode() {
        return klass.hashCode();
    }
}
