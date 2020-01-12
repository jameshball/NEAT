public class NodeGene {
  private final NodeType type;
  private final int id;

  public NodeGene(NodeType type, int id) {
    this.type = type;
    this.id = id;
  }

  public NodeType getType() {
    return type;
  }

  public int getId() {
    return id;
  }
}
