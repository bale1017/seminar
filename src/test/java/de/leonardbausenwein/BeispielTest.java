package de.leonardbausenwein;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(JUnitQuickcheck.class)
public class BeispielTest {

  @Property
  public void test1(String s) {
    System.out.println(s);
  }

  @Property
  public void test(@From(StringGen.class) String s) {
    Assertions.assertEquals(s.getBytes().length, s.length());
  }
}
