package de.leonardbausenwein;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringGen extends Generator<String> {

  public StringGen() {
    super(String.class);
  }

  @Override
  public String generate(SourceOfRandomness r, GenerationStatus generationStatus) {

    // maximal 10 zeichen
    int len = r.nextInt(40);
    int spaceCount = r.nextInt(10);
    spaceCount = Math.min(spaceCount, len);

    byte[] result = "-".repeat(len).getBytes(StandardCharsets.UTF_8);

    // Fülle mit bis zu 10 Leerzeichen
    for (int i = 0; i < spaceCount; i++) {
      result[i] = ' ';
    }
    int spaceChainLen = 0;

    // remove all chains of spaces
    String s = new String(result);
    while (true) {
      Matcher matcher = Pattern.compile(" {6,}").matcher(s);
      if (!matcher.find(0)) {
        break;
      }

      int spaces = matcher.group().length();
      int offset = r.nextInt(spaces);
      s = matcher.replaceFirst(" ".repeat(offset) + randomChar(r) + " ".repeat(spaces - offset - 1));
    }
    result = s.getBytes();

    // Fülle mit Kleinbuchstaben
    for (int i = 0; i < result.length; i++) {
      if (result[i] == '-') {
        result[i] = (byte) randomChar(r);
      }
    }
    return new String(result);
  }

  private char randomChar(SourceOfRandomness sourceOfRandomness) {
    return sourceOfRandomness.nextChar('a', 'z');
  }
}
