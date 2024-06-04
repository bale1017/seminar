package de.leonardbausenwein.seminar;

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

    // maximal 40 zeichen
    int len = r.nextInt(41);
    // maximal 10 leerzeichen
    int spaceCount = r.nextInt(11);
    spaceCount = Math.min(spaceCount, len);

    // Erzeuge ein Byte-Array der passenden Länge (gefüllt mit 0en).
    // Am Ende der Ausführung sollen keine 0-Werte vorhanden sein.
    byte[] result = new byte[len];

    // Fülle mit bis zu 10 Leerzeichen an beliebiger Stelle
    for (int i = 0; i < spaceCount; i++) {
      result[r.nextInt(len)] = ' ';
    }

    // Entferne alle Leerzeichen-Ketten mit mehr als 5 Zeichen.
    String s = new String(result);
    while (true) {
      // Für faire Verteilung der Leerzeichen wird "greedy" nach Space-Ketten gesucht und zufällig zerlegt.
      // Das passiert auf Kosten der Performance, eine Alternative wäre, jedes 6. Leerzeichen zu eliminieren.
      // Leerzeichen wären dann allerdings statistisch öfter am Anfang des Strings zu finden.
      Matcher matcher = Pattern.compile(" {6,}").matcher(s);
      if (!matcher.find(0)) {
        break;
      }

      int spaces = matcher.group().length();
      int offset = r.nextInt(spaces);
      s = matcher.replaceFirst(" ".repeat(offset) + randomChar(r) + " ".repeat(spaces - offset - 1));
    }
    result = s.getBytes();

    // Ersetze leere Array Felder mit Kleinbuchstaben
    for (int i = 0; i < result.length; i++) {
      if (result[i] == 0) {
        result[i] = (byte) randomChar(r);
      }
    }
    return new String(result);
  }

  private char randomChar(SourceOfRandomness sourceOfRandomness) {
    return sourceOfRandomness.nextChar('a', 'z');
  }
}
