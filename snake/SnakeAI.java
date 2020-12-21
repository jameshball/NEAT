class SnakeAI {

  private static final int GRID_WIDTH = 40;
  private static final int GRID_HEIGHT = 40;

  private static final int POPULATION_SIZE = 500;
  private static final int NUM_INPUTS = 24;
  private static final int NUM_OUTPUTS = 4;

  public static void main(String[] args) {
    Population pop = new Population(POPULATION_SIZE, NUM_INPUTS, NUM_OUTPUTS, new Level(GRID_WIDTH, GRID_HEIGHT));

    while (true) {
      pop.update();
    }
  }
}
