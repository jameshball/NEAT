import java.util.Objects;

class ConnectionGene {

  private final int in;
  private final int out;

  public ConnectionGene(int in, int out) {
    assert in >= 0;
    assert out >= 0;

    this.in = in;
    this.out = out;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    ConnectionGene gene = (ConnectionGene) obj;
    return in == gene.in && out == gene.out;
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, out);
  }

  public int getIn() {
    return in;
  }

  public int getOut() {
    return out;
  }
}
