package fr.pturpin.quickcheck.functional;

/**
 * Created by pturpin on 05/06/2017.
 */
@FunctionalInterface
public interface Function3<A, B, C, R> {

  R apply(A a, B b, C c);

}
