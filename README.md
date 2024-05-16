# Quickcheck in Java

## Software

Quickcheck ist ein für Haskel designtes Test-Framework, welches das Testen mithilfe
generierter Properties erlaubt, also Tests auf einer zufällig generierten Datenmenge
anstatt weniger ausgewähler Beispieldaten.

Für die Java Umgebung gibt es eine JUnit Test Erweiterung, die ermöglicht,
Unit-Tests mit generierten Properties auszuführen.

Die hierfür verwendete Software nennt
sich [JUnit-Quickcheck](https://pholser.github.io/junit-quickcheck/site/1.0/index.html).

## JUnit Tests mit Properties

Wir lassen einen Beispieltest laufen, in dem Strings mithilfe eines Generators
bereitgestellt werden. Dies wird durch die `@Property` Annotation gekennzeichnet.

Standardmäßig sind keine Generatoren implementiert. Es gibt jedoch eine Bibliothek
mit Implementierungen zu den gängigsten Java Datentypen.

```Java

@RunWith(JUnitQuickcheck.class)
public class BeispielTest {

  @Property
  public void test(String s) {
    Assertions.assertEquals(s.getBytes().length, s.length());
  }
}
```

Wir definieren außerdem mithilfe der `@RunWith` Annotation einen JUnit Test Runner.
Wir möchten Tests in dieser Klasse mit dem Quickcheck Test Runner ausführen.

## Zuweisung von Generatoren

Generatoren müssen als solche gekennzeichnet werden.
Generatoren für eine selbst entworfene Klasse lassen sich durch Benennung und Platzierung laden.

`main/de.leonardbausenwein.seminar.MyClass`

lässt sich durch

`test/de.leonardbausenwein.seminar.MyClassGen`

generieren.

JUnit-Quickcheck erlaubt auch die Implementierung eines Services
in `META-INF/services/com.pholster.junit.quickcheck.generator.Generator`

Eine Service Registrierung ist vorallem Sinnvoll bei fremden Klassen wie String.

Zuletzt lässt sich ein Generator auch Explizit mit der `@From` Annotation setzen.

```Java
  @Property
  public void test(@From(MyStringGenerator.class) String s) {
    Assertions.assertEquals(s.getBytes().length, s.length());
  }
```

## Beispiel eines String Generators

Ein Generator für einen bestimmten Typ erweitert eine Basisklasse
um eine `generate` Methode, welche das zu verwendente Property bereitstellt.
```Java
public class StringGen extends Generator<String> {

  public StringGen() {
    super(String.class);
  }

  @Override
  public String generate(SourceOfRandomness rand, GenerationStatus generationStatus) {
    return rand.nextChar('a', 'z');
  }
}
```

Ein String Generator mit den Bedingungen:
- 0-40 Kleinbuchstaben oder Leerzeichen
- davon 0-10 Leerzeichen
- Nicht mehr als 5 Leerzeichen in Folge
könnte wie folgt aussehen:

```Java
  @Override
  public String generate(SourceOfRandomness r, GenerationStatus generationStatus) {

    // maximal 10 zeichen
    int len = r.nextInt(40);
    int spaceCount = r.nextInt(10);
    spaceCount = Math.min(spaceCount, len);

    // Erzeuge einen String der passenden Länge bestehend aus '-' Symbolen.
    byte[] result = "-".repeat(len).getBytes(StandardCharsets.UTF_8);

    // Fülle mit bis zu 10 Leerzeichen
    for (int i = 0; i < spaceCount; i++) {
      result[i] = ' ';
    }
    int spaceChainLen = 0;

    // Entferne alle Leerzeichen-Ketten mit mehr als 5 Zeichen.
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

    // Ersetze '-' Symbole mit Kleinbuchstaben
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
```

## Beispiel anhand von Listen

Eine Besonderheit von List Klassen ist das TypArgument, welches die Typen der Objekte in der
Liste beschreibt.

JUnit-Quickcheck bietet eine Möglichkeit, diese Typen ebenfalls zu generieren.
Es analysiert die Typinformationen mithilfe von Reflection und sucht nach registrierten
Generatoren für den Typ der Listenelemente.

`List<String>` verwendet also einen Generator für Listen, welcher einen Generator für Strings
benutzt, um die Liste mit Elementen zu füllen.

Ein Generator mit dem festen Typen String muss keine Typparameter analysieren und könnte so aussehen:
```Java
public class StringListGen extends Generator<List> {

  private final StringGen stringGen = new StringGen();

  public StringListGen() {
    super(List.class);
  }

  @Override
  public List<String> generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
    List<String> objects = new ArrayList<>();
    int size = sourceOfRandomness.nextInt(10);
    for (int i = 0; i < size; i++) {
      objects.add(stringGen.generate(sourceOfRandomness, generationStatus));
    }
    return objects;
  }
}
```

Wir verwenden eine Instanz des vorher definierten String Generators, um unsere Liste mit Elementen zu füllen.

## Null-Werte

Java verfügt über die Annotationen `@NotNull` und `@Nullable`, die verwendet werden um Parameter oder
Rückgabewerte explizit als "möglicherweise null" oder "niemals null" kennzeichnen zu können.

```Java
  @Property
  public void test(@Nullable String s) {
    if (s != null) Assertions.assertEquals(s.getBytes().length, s.length());
  }
```
Hier wird die `@Nullable` Annotation entsprechend ausgewertet und der Parameter s ist möglicherweise null.
