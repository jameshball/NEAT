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

  public void setWeight(float weight) {
    this.weight = weight;
  }

  public void enable() {
    enabled = true;
  }

  public void disable() {
    enabled = false;
  }

  public int getIn() {
    return gene.getIn();
  }

  public int getOut() {
    return gene.getOut();
  }
}
