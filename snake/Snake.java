import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* Snake holds all the information about the Snake, including its current position, location of all parts
of its tail and whether it is still alive. */
public class Snake {

  /* By default, the snake moves right */
  private static final Vector2 DEFAULT_DIRECTION = new Vector2(1, 0);

  private final List<Vector2> body;

  private Vector2 head;
  private Vector2 direction;
  private boolean dead;
  private final Random rng;

  public Snake() {
    this.rng = new Random();
    /* This resets the snake's head to a random position at least 1 square away from the edges. */
    this.head = new Vector2(rng.nextInt(SnakeAI.gridX - 1) + 1, rng.nextInt(SnakeAI.gridY - 1) + 1);
    this.dead = false;
    this.body = new ArrayList<>();
    this.body.add(new Vector2(head.x, head.y));
    this.direction = DEFAULT_DIRECTION;
  }

  public int length() {
    return body.size();
  }

  /* This method is executed every frame. 'direction' is updated externally and corresponds to the next
  direction the snake will move in. 'direction' is added to the location of the snake's head.
  It also checks if the snake has hit its tail or gone out of the bounds of the grid. */
  public void update() {
    head.add(direction);

    if (isTail(head) || !Level.withinBounds(head)) {
      dead = true;
    }
  }

  /* This method moves the snake by removing the last element in their tail and adding the location of the
  snake's head. */
  public void move() {
    body.remove(0);
    body.add(new Vector2(head.x, head.y));
  }

  /* This method extends the snake's body by adding the new position of the snake's head, without removing
  the end of its tail. */

  public void extend() {
    body.add(new Vector2(head.x, head.y));
  }
  /* Compares each part of the snake's tail with the new head position. If they are equal, the snake has
  hit its tail. */

  private boolean isTail(Vector2 vector) {
    for (int i = 0; i < body.size() - 1; i++) {
      Vector2 part = body.get(i);
      if (part.x == vector.x && part.y == vector.y) {
        return true;
      }
    }

    return false;
  }
  /* Updates the snake's direction so it now points in the new direction. */

  public void point(Vector2 dir) {
    direction = dir;
  }
  public void kill() {
    dead = true;
  }

  public boolean isDead() {
    return dead;
  }

  public float getX() {
    return head.x;
  }

  public float getY() {
    return head.y;
  }

  public Vector2 tail() {
    return body.get(0);
  }

  public Vector2 head() {
    return head;
  }
}
