package de.leonardbausenwein.seminar;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.util.ArrayList;
import java.util.List;

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
