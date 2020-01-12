import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class Genome {
  private List<NodeGene> nodeGenes;
  private List<Connection> connections;
  private Random rng;

  private static final float EXCESS_COEFFICIENT = 1.0f;
  private static final float DISJOINT_COEFFICIENT = 1.0f;
  private static final float WEIGHT_DIFF_COEFFICIENT = 0.4f;

  public final int INPUT_COUNT;
  public final int OUTPUT_COUNT;

  public Genome(int inputCount, int outputCount, HashMap<ConnectionGene, Integer> innovations) {
    this.INPUT_COUNT = inputCount;
    this.OUTPUT_COUNT = outputCount;
    this.nodeGenes = new ArrayList<>();
    this.connections = new ArrayList<>();
    this.rng = new Random();

    initialiseGenome(innovations);
  }

  public static float compatibilityDistance(Genome genome1, Genome genome2) {
    return 0;
  }

  private void initialiseGenome(HashMap<ConnectionGene, Integer> innovations) {
    for (int i = 0; i < INPUT_COUNT; i++) {
      nodeGenes.add(new NodeGene(NodeType.INPUT, i));
    }

    for (int i = INPUT_COUNT; i < nodeCount(); i++) {
      nodeGenes.add(new NodeGene(NodeType.OUTPUT, i));

      for (int j = 0; j < INPUT_COUNT; j++) {
        // The ids of the nodeGenes in this case are just the indexes j and i.
        ConnectionGene gene = new ConnectionGene(j, i);
        float randomWeight = 2 * rng.nextFloat() - 1;

        Population.addInnovation(gene, innovations);
        connections.add(new Connection(gene, randomWeight));
      }
    }
  }

  public NodeGene getNodeGene(int id) {
    return nodeGenes.get(id);
  }

  public Connection getConnection(int index) {
    return connections.get(index);
  }

  public boolean containsNode(NodeGene node) {
    return nodeGenes.contains(node);
  }

  private int nodeCount() {
    return INPUT_COUNT + OUTPUT_COUNT;
  }
}
