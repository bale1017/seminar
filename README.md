# Quickcheck in Java

## Software

Quickcheck ist ein für Haskell designtes Test-Framework, welches das Testen mithilfe
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

## Verwendung: Zuweisung von Generatoren

Generatoren müssen als solche gekennzeichnet werden.
Generatoren für eine selbst entworfene Klasse lassen sich durch Benennung und Platzierung laden.

`main/de.leonardbausenwein.seminar.MyClass`

lässt sich durch

`test/de.leonardbausenwein.seminar.MyClassGen`

generieren.

JUnit-Quickcheck erlaubt auch die Implementierung eines Services
in `META-INF/services/com.pholster.junit.quickcheck.generator.Generator`

Eine Service Registrierung ist vorallem Sinnvoll bei externen Klassen wie String, um innerhalb der eigenen Package-Benennung zu arbeiten.

Zuletzt lässt sich ein Generator auch Explizit mit der `@From` Annotation setzen.

```Java
  @Property
  public void test(@From(MyStringGenerator.class) String s) {
    Assertions.assertEquals(s.getBytes().length, s.length());
  }
```

Ist ein Generator korrekt zugewiesen, lässt er sich in JUnit Tests verwenden.

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

Javax verfügt über die Annotation `@Nullable`, die verwendet wird um Parameter oder
Rückgabewerte explizit als "möglicherweise null" kennzeichnen zu können.

```Java
  @Property
  public void test(@Nullable String s) {
    if (s != null) Assertions.assertEquals(s.getBytes().length, s.length());
  }
```
Hier wird die `@Nullable` Annotation entsprechend ausgewertet und der Parameter s ist möglicherweise null.
Dies gilt nur für `javax.annotation.Nullable` und ist nicht zu verwechseln mit den Jetbrains Intellij Annotationen,
die oft schon im Dependency-Tree zu finden sind.

Generatoren können auch selbstständig in ihrer Implementierung einen null Wert zurückgeben.
Sind Parameter zu diesen Generatoren mit `@NotNull` annotiert, scheitert die Ausführung der Tests.

Null wird bei Verwendung von `@Nullable` mit einer 50% Wahrscheinlichkeit erzeugt.
Es empfiehlt sich also vorallem bei wenigen Parametern die Verwendung von
`@NullAllowed(probability = 0.05f)`.

Für Null-Werte gibt es keine weiteren Beschränkungen: die Verantwortung für eine sinnvolle
Rückgabe von Null-Werten liegt beim Entwickler des Generators.
Optimaler Weise gibt ein Generator selbst nicht null zurück, da die Freiheit dann beim Entwickler
des Tests liegt, indem dieser entsprechende Annotationen verwendet.

Generatoren können und sollten jedoch Beispielsweise Kollektionen mit Null-Werten füllen.
