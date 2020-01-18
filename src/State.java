public interface State {
  float evaluateFitness();
  void update(float[] inputs);
  float[] generateGenomeInputs();
  boolean hasEnded();
  State reset();
  State deepCopy();
}