// Blank state for creating genomes without a state for testing purposes.
public class BlankState implements State {
  @Override
  public float evaluateFitness() {
    return 0;
  }

  @Override
  public void update(float[] inputs) {}

  @Override
  public float[] getGenomeInputs() {
    return new float[0];
  }

  @Override
  public boolean hasEnded() {
    return false;
  }

  @Override
  public State reset() {
    return this;
  }

  @Override
  public State deepCopy() {
    return this;
  }
}
