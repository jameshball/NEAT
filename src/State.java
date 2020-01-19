public interface State {
  float evaluateFitness();
  void update(float[] inputs);
  float[] getGenomeInputs();
  boolean hasEnded();
  State reset();
  State deepCopy();
}