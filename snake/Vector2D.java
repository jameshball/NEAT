public class Vector2D {
  float x;
  float y;

  Vector2D(float x, float y) {
    this.x = x;
    this.y = y;
  }

  Vector2D add(Vector2D v) {
    x += v.x;
    y += v.y;

    return this;
  }
}
