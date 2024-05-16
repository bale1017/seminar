package de.leonardbausenwein.seminar;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;

@RunWith(JUnitQuickcheck.class)
public class StringGenTest {

  @Property
  public void testNoMoreThan5Spaces(@From(StringGen.class) String s) {
    Assertions.assertFalse(s.contains("      "));
  }

  @Property
  public void testMax10Spaces(@From(StringGen.class) String s) {
    int i = 0;
    for (byte c : s.getBytes(StandardCharsets.UTF_8)) {
      if (c == ' ') {
        i++;
      }
    }
    Assertions.assertTrue(i <= 10);
  }

  @Property
  public void testMax40Chars(@From(StringGen.class) String s) {
    Assertions.assertTrue(s.length() <= 40);
  }

  @Property
  public void testOnlyLowerCase(@From(StringGen.class) String s) {
    Assertions.assertTrue(s.matches("[a-z ]*"));
  }
}
