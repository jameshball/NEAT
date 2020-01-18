public interface State {
  float evaluateFitness();
  void update(float[] inputs);
  float[] generateGenomeInputs();
}