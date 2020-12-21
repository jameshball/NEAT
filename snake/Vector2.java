public class Vector2 {
  public float x;
  public float y;

  public Vector2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vector2 add(Vector2 v) {
    x += v.x;
    y += v.y;

    return this;
  }

  public Vector2 copy() {
    return new Vector2(x, y);
  }
}
