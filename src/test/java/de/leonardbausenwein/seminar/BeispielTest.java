package de.leonardbausenwein.seminar;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;

@RunWith(JUnitQuickcheck.class)
public class BeispielTest {

  @Property
  public void testNull(Object object) {
    Assertions.assertNull(object);
  }

  @Property
  public void testNullObject(@Nullable UUID uuid) {
    if (uuid == null) {
      System.out.println("null");
    }
  }

  @Property
  public void testNullQuantity(@Nullable String a) {
    if (a == null) {
      System.out.println((String) null);
    }
  }

  @Property
  public void testNullQuantity(@Nullable String a, @Nullable String b, @Nullable String c) {
    if (a == null || b == null || c == null) {
      System.out.println(a + " ; " + b + " ; " + c);
    }
  }

  @Property
  public void test1(String s) {
    System.out.println(s);
  }

  @Property
  public void test(@From(StringGen.class) String s) {
    Assertions.assertEquals(s.getBytes().length, s.length());
  }

  @Property
  public void test(@From(StringListGen.class) List<String> l) {
    System.out.println(l);
  }
}
