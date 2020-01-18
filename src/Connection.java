public class Connection {
  private ConnectionGene gene;
  private float weight;
  private boolean enabled;

  public Connection(int in, int out, float weight, boolean enabled) {
    assert in >= 0;
    assert out >= 0;

    this.gene = new ConnectionGene(in, out);
    this.enabled = enabled;

    setWeight(weight);
  }

  public Connection(int in, int out, float weight) {
    this(in, out, weight, true);
  }

  public Connection(ConnectionGene gene, float weight) {
    this(gene.getIn(), gene.getOut(), weight);
  }

  public Connection copy() {
    return new Connection(getIn(), getOut(), weight, enabled);
  }

  public float getWeight() {
    return weight;
  }

  public void setWeight(float weight) {
    // Limits weights to the range -1.0 -> 1.0
    if (weight > 1) {
      weight = 1;
    } else if (weight < -1) {
      weight = -1;
    }

    this.weight = weight;
  }

  public void enable() {
    enabled = true;
  }

  public void disable() {
    enabled = false;
  }

  public boolean isDisabled() {
    return !enabled;
  }

  public int getIn() {
    return gene.getIn();
  }

  public int getOut() {
    return gene.getOut();
  }

  public ConnectionGene getGene() {
    return gene;
  }
}
