package fr.pturpin.quickcheck.annotation;

import fr.pturpin.quickcheck.registry.Registries;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Created by turpif on 08/06/17.
 */

/**
 * Market annotation to indicate that the annotated method may be used as a generator
 * when using {@link Registries#forClass(Class)}
 *
 * The annotated method should follow few rules given in {@link Registries#forMethod(Method)}.
 *
 * @see Registries#forClass(Class)
 * @see Registries#forMethod(Method)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Gen {
}
