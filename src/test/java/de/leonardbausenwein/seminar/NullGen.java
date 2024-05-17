package de.leonardbausenwein.seminar;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class NullGen extends Generator<Object> {

  public NullGen() {
    super(Object.class);
  }

  @Override
  public Object generate(SourceOfRandomness random, GenerationStatus status) {
    return null;
  }
}
