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

`main/de.leonardbausenwein.myapp.MyClass`

lässt sich durch

`test/de.leonardbausenwein.myapp.MyClassGen`

generieren.

JUnit-Quickcheck erlaubt auch die Implementierung eines Services
in `META-INF/services/com.pholster.junit.quickcheck.generator.Generator`

Eine Service Registrierung ist vorallem Sinnvoll bei fremden Klassen wie String.

Zuletzt lässt sich ein Generator auch Explizit mit der `@From` Annotation setzen.

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