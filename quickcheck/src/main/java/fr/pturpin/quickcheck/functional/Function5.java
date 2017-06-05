package fr.pturpin.quickcheck.functional;

/**
 * Created by pturpin on 05/06/2017.
 */
@FunctionalInterface
public interface Function5<A, B, C, D, E, R> {

  R apply(A a, B b, C c, D d, E e);

}
