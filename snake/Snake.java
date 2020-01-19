import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* Snake holds all the information about the Snake, including its current position, location of all parts
of its tail and whether it is still alive. */
class Snake {
  Vector2D pos;
  List<Vector2D> body;
  Vector2D direction;
  boolean dead;
  Random rng = new Random();

  Snake() {
    /* This resets the snake's head to a random position at least 1 square away from the edges. */
    pos = new Vector2D(rng.nextInt(NEATClient.gridX - 1) + 1, rng.nextInt(NEATClient.gridY - 1) + 1);
    dead = false;
    body = new ArrayList<>();
    body.add(new Vector2D(pos.x, pos.y));
    /* The default direction moves right */
    direction = new Vector2D(1, 0);
  }

  /* This method is executed every frame. 'direction' is updated externally and corresponds to the next
  direction the snake will move in. 'direction' is added to the location of the snake's head.
  It also checks if the snake has hit its tail or gone out of the bounds of the grid. */
  void update() {
    pos.add(direction);

    if (isTail(pos) || pos.x < 0 || pos.x >= NEATClient.gridX || pos.y < 0 || pos.y >= NEATClient.gridY) {
      dead = true;
    }
  }

  /* This method moves the snake by removing the last element in their tail and adding the location of the
  snake's head. */
  void move() {
    body.remove(0);
    body.add(new Vector2D(pos.x, pos.y));
  }

  /* This method extends the snake's body by adding the new position of the snake's head, without removing
  the end of its tail. */
  void extend() {
    body.add(new Vector2D(pos.x, pos.y));
  }

  /* Compares each part of the snake's tail with the new head position. If they are equal, the snake has
  hit its tail. */
  boolean isTail(Vector2D vector) {
    for (int i = 0; i < body.size() - 1; i++) {
      Vector2D part = body.get(i);
      if (part.x == vector.x && part.y == vector.y) {
        return true;
      }
    }

    return false;
  }
}
