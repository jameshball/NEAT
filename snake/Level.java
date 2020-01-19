import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/* The Level class holds all details about the current state of the game, including the Snake,
Apple and score. It has the functionality to update the grid each frame. */
class Level implements State {
  private GridState[][] grid;
  private Snake snake;
  private Vector2D apple;
  int score;
  private int movesSinceLastApple = 0;
  Random rng = new Random();

  Level() {
    snake = new Snake();
    resetGrid();
    resetApple();
    rng = new Random();
  }

  /* Resets the grid to all EMPTY squares. */
  private void resetGrid() {
    grid = new GridState[NEATClient.gridX][NEATClient.gridY];

    for (int i = 0; i < NEATClient.gridX; i++) {
      for (int j = 0; j < NEATClient.gridY; j++) {
        grid[i][j] = GridState.EMPTY;
      }
    }
  }

  /* Generates a new apple position, which is a random location that is not taken up by the snake. */
  private void resetApple() {
    int emptyGridSpaces = NEATClient.gridX * NEATClient.gridY - snake.body.size();
    int randomFreeSpace = rng.nextInt(emptyGridSpaces);
    int emptySpaceCount = 0;

    for (int i = 0; i < NEATClient.gridX; i++) {
      for (int j = 0; j < NEATClient.gridY; j++) {
        if (grid[i][j] == GridState.EMPTY) {
          emptySpaceCount++;

          if (emptySpaceCount >= randomFreeSpace) {
            apple = new Vector2D(i, j);
            set(apple, GridState.APPLE);
            return;
          }
        }
      }
    }
  }

  @Override
  public void update(float[] input) {
    if (!hasEnded()) {
      /* This code looks at the strongest output from the NN to decide what move to make. */
      float max = input[0];
      int maxIndex = 0;

      for (int i = 1; i < input.length; i++) {
        if (input[i] > max) {
          max = input[i];
          maxIndex = i;
        }
      }

      Vector2D dir = new Vector2D(0, 0);

      switch (maxIndex) {
        case 0:
          dir = new Vector2D(0, 1);
          break;
        case 1:
          dir = new Vector2D(1, 0);
          break;
        case 2:
          dir = new Vector2D(0, -1);
          break;
        case 3:
          dir = new Vector2D(-1, 0);
          break;
      }

      /* Changes the direction of the snake to the newly decided direction. */
      snake.direction = dir;

      updateLevel();
    }
  }

  /* The update method moves the head of the snake and checks if it has died. If the snake has eaten
  an apple, it will grow, otherwise it will move to the new position. This only executes if the
  snake is alive. */
  void updateLevel() {
    snake.update();

    /* If the snake has run out of moves for this apple... */
    if (movesSinceLastApple > NEATClient.allowedMoves[snake.body.size()]) {
      snake.dead = true;
    }

    if (!snake.dead) {
      /* If the snake eats an apple... */
      if (snake.pos.x == apple.x && snake.pos.y == apple.y) {
        /* Increase the score and size of the snake, generate a new apple and change the
        nextAppleMoves value. */
        snake.extend();
        resetApple();
        score++;
        movesSinceLastApple = 0;
        updateGrid(true, new Vector2D(0, 0));
      } else {
        Vector2D tail = snake.body.get(0);
        updateGrid(false, tail);
        snake.move();
      }

      movesSinceLastApple++;
    }
  }

  /* Updates the snake's position in the grid. */
  private void updateGrid(boolean appleEaten, Vector2D tailPos) {
    if (!appleEaten) {
      set(tailPos, GridState.EMPTY);
    }

    set(snake.pos, GridState.SNAKE);
  }

  /* Sets the element stored at 'pos' in the grid array to the value 'objectType'. */
  private void set(Vector2D pos, GridState objectType) {
    grid[(int) pos.x][(int) pos.y] = objectType;
  }

  /* Returns a list of three numbers. They represent the distance to the snake's body, the apple
  and the walls of the grid. */
  private List<Float> snakeLook(Vector2D direction) {
    /* I need to create a deep copy of snake.pos because I will be modifying it in this method. */
    Vector2D snakePos = new Vector2D(snake.pos.x, snake.pos.y);
    Float[] vision = new Float[3];
    int distance = 1;

    Arrays.fill(vision, 0.0f);

    /* Move snakePos in the direction of the dir vector specified and continue if it is still
    within the bounds of the grid. */
    while (withinBounds(snakePos.add(direction))) {
      /* If snakePos comes in contact with the snake's body and vision[0] is unassigned... */
      if (grid[(int) snakePos.x][(int) snakePos.y] == GridState.SNAKE && vision[0] == 0) {
        vision[0] = 1.0f / distance;
      }

      /* If snakePos comes in contact with an apple and vision[1] is unassigned... */
      if (grid[(int) snakePos.x][(int) snakePos.y] == GridState.APPLE && vision[1] == 0) {
        vision[1] = 1.0f;
      }

      distance++;
    }

    /* Sets the distance to the wall of the grid. */
    vision[2] = 1.0f / distance;

    return Arrays.asList(vision);
  }

  /* This uses the snakeLook() method to look in eight directions around the snake (i.e. NESW,
  and all diagonals). This forms as the input to the player's neural network. */
  @Override
  public float[] getGenomeInputs() {
    ArrayList<Float> vision = new ArrayList<>();

    for (Vector2D direction : NEATClient.directions) {
      vision.addAll(snakeLook(direction));
    }

    /* Converts the vision list into an array. */
    float[] arr = new float[vision.size()];

    for (int i = 0; i < vision.size(); i++) {
      arr[i] = vision.get(i);
    }

    return arr;
  }

  /* Returns true if the input vector is within the bounds of the grid. */
  private boolean withinBounds(Vector2D loc) {
    return !(loc.x > NEATClient.gridX - 1 || loc.x < 0 || loc.y > NEATClient.gridY - 1 || loc.y < 0);
  }

  @Override
  public float evaluateFitness() {
    return score * score;
  }

  @Override
  public boolean hasEnded() {
    return snake.dead;
  }

  @Override
  public State reset() {
    return new Level();
  }

  @Override
  public State deepCopy() {
    return this;
  }
}
