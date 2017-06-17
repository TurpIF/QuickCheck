package fr.pturpin.quickcheck.identifier;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import static fr.pturpin.quickcheck.identifier.Identifiers.classId;

/**
 * Created by turpif on 12/06/17.
 */
public class Identifiers_UT {
  
  @Test
  public void classIdShouldDetectParametrizedTypes() {
    Assert.assertEquals(0, classId(String.class).getNbParametrizedType());
    Assert.assertEquals(1, classId(Collection.class).getNbParametrizedType());
    Assert.assertEquals(2, classId(Map.class).getNbParametrizedType());
    Assert.assertEquals(3, classId(BiFunction.class).getNbParametrizedType());
  }

}
