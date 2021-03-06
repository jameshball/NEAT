import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/* The Level class holds all details about the current state of the game, including the Snake,
Apple and score. It has the functionality to update the grid each frame. */
public class Level implements State {

  /* Links indexes of the input array in update() to vector directions. */
  private static final List<Vector2> DIRECTIONS = List.of(Vector2.NORTH, Vector2.EAST, Vector2.SOUTH, Vector2.WEST);
  /* Directions the snake should look in when retrieving inputs. */
  private static final List<Vector2> LOOKING_DIRECTIONS = List.of(
    Vector2.NORTH,
    Vector2.NORTHEAST,
    Vector2.EAST,
    Vector2.SOUTHEAST,
    Vector2.SOUTH,
    Vector2.SOUTHWEST,
    Vector2.WEST,
    Vector2.NORTHWEST
  );

  private final Snake snake;
  private final Random rng;

  private final int width;
  private final int height;
  private final int[] allowedMoves;

  private GridState[][] grid;
  private Vector2 apple;
  private int score;
  private int movesSinceLastApple = 0;

  public Level(int width, int height) {
    this.rng = new Random();
    this.width = width;
    this.height = height;
    this.allowedMoves = new int[width * height + 1];

    for (int i = 0; i < allowedMoves.length; i++) {
      allowedMoves[i] = (int) (200 * (Math.log(i) / Math.log(3)) + 300);
    }
    resetGrid();
    this.snake = new Snake(this);
    resetApple();
  }

  /* Resets the grid to all EMPTY squares. */
  private void resetGrid() {
    grid = new GridState[width][height];

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        grid[i][j] = GridState.EMPTY;
      }
    }
  }

  /* Generates a new apple position, which is a random location that is not taken up by the snake. */
  private void resetApple() {
    int numEmptySpaces = width * height - snake.length();
    int randomFreeSpace = rng.nextInt(numEmptySpaces);
    int emptySpaceCount = 0;

    /* Loops through the grid and finds the randomFreeSpace within the grid that was randomly chosen
     * according to the numEmptySpaces. */
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if (grid[i][j] == GridState.EMPTY) {
          emptySpaceCount++;

          if (emptySpaceCount >= randomFreeSpace) {
            apple = new Vector2(i, j);
            set(apple, GridState.APPLE);
            return;
          }
        }
      }
    }

    /* Unreachable */
    throw new RuntimeException("Apple location should have been chosen!");
  }

  @Override
  public void update(float[] input) {
    /* This code looks at the strongest output from the NN to decide what move to make. */
    float max = input[0];
    int maxIndex = 0;

    for (int i = 1; i < input.length; i++) {
      if (input[i] > max) {
        max = input[i];
        maxIndex = i;
      }
    }

    Vector2 dir = DIRECTIONS.get(maxIndex);
    /* Changes the direction of the snake to the newly decided direction. */
    snake.point(dir);
    updateLevel();
  }

  /* The update method moves the head of the snake and checks if it has died. If the snake has eaten
  an apple, it will grow, otherwise it will move to the new position. This only executes if the
  snake is alive. */
  private void updateLevel() {
    snake.update();

    /* If the snake has run out of moves for this apple... */
    if (movesSinceLastApple > allowedMoves[snake.length()]) {
      snake.kill();
    }

    if (!snake.isDead()) {
      /* If the snake eats an apple... */
      if (snake.head().equals(apple)) {
        /* Increase the score and size of the snake, generate a new apple and change the
        nextAppleMoves value. */
        snake.extend();
        resetApple();
        score++;
        movesSinceLastApple = 0;
        updateGrid(true, new Vector2());
      } else {
        Vector2 tail = snake.tail();
        updateGrid(false, tail);
        snake.move();
      }

      movesSinceLastApple++;
    }
  }

  /* Updates the snake's position in the grid. */
  private void updateGrid(boolean appleEaten, Vector2 tailPos) {
    if (!appleEaten) {
      set(tailPos, GridState.EMPTY);
    }

    set(snake.head(), GridState.SNAKE);
  }

  /* Sets the element stored at 'pos' in the grid array to the value 'objectType'. */
  private void set(Vector2 pos, GridState objectType) {
    grid[(int) pos.x][(int) pos.y] = objectType;
  }

  /* Returns a list of three numbers. They represent the distance to the snake's body, the apple
  and the walls of the grid. */
  private List<Float> snakeLook(Vector2 direction) {
    /* I need to create a deep copy of snake.pos because I will be modifying it in this method. */
    Vector2 head = snake.head().copy();
    Float[] vision = new Float[3];
    int distance = 1;

    Arrays.fill(vision, 0.0f);

    /* Move head in the direction of the dir vector specified and continue if it is still
    within the bounds of the grid. */
    while (withinBounds(head.add(direction))) {
      /* If head comes in contact with the snake's body and vision[0] is unassigned... */
      if (grid[(int) head.x][(int) head.y] == GridState.SNAKE && vision[0] == 0) {
        vision[0] = 1.0f / distance;
      }

      /* If head comes in contact with an apple and vision[1] is unassigned... */
      if (grid[(int) head.x][(int) head.y] == GridState.APPLE && vision[1] == 0) {
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

    for (Vector2 direction : LOOKING_DIRECTIONS) {
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
  public boolean withinBounds(Vector2 pos) {
    return pos.x <= width - 1 && pos.x >= 0 && pos.y <= height - 1 && pos.y >= 0;
  }

  @Override
  public float evaluateFitness() {
    return score * score;
  }

  @Override
  public boolean hasEnded() {
    return snake.isDead();
  }

  @Override
  public State reset() {
    return new Level(width, height);
  }

  @Override
  public State deepCopy() {
    return this;
  }

  @Override
  public float activate(float x) {
    return State.sigmoid(x);
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  private enum GridState {
    SNAKE,
    EMPTY,
    APPLE
  }
}
