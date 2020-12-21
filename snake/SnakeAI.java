class SnakeAI {
  static int populationSize = 500;
  static int gridX = 40;
  static int gridY = 40;
  static int[] allowedMoves = new int[gridX * gridY + 1];
  static Vector2[] directions =
      new Vector2[] {
        new Vector2(0, 1),
        new Vector2(1, 1),
        new Vector2(1, 0),
        new Vector2(1, -1),
        new Vector2(0, -1),
        new Vector2(-1, -1),
        new Vector2(-1, 0),
        new Vector2(-1, 1)
      };

  public static void main(String[] args) {
    for (int i = 0; i < allowedMoves.length; i++) {
      allowedMoves[i] = (int) (200 * (Math.log(i) / Math.log(3)) + 300);
    }

    Population pop = new Population(populationSize, 24, 4, new Level());

    while (true) {
      pop.update();
    }
  }
}
