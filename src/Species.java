public class Species {
  private static final int GENERATIONS_TO_STAGNATE = 15;

  private float maxFitness;
  private int generationLastImproved;
  private int size;

  public Species(int currentGeneration) {
    maxFitness = 0;
    generationLastImproved = currentGeneration;
    size = 0;
  }

  public boolean isStagnant(int currentGeneration) {
    return currentGeneration - generationLastImproved >= GENERATIONS_TO_STAGNATE;
  }

  public void bestFitnessInSpecies(float fitness, int currentGeneration) {
    if (fitness > maxFitness) {
      generationLastImproved = currentGeneration;
      maxFitness = fitness;
    }
  }

  public void add() {
    size++;
  }

  public void remove() {
    size--;
  }

  public int size() {
    return size;
  }
}
