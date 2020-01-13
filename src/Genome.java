import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class Genome {
  private List<NodeGene> nodeGenes;
  private List<Connection> connections;
  private Random rng;
  private int species;

  private static final float EXCESS_COEFFICIENT = 1.0f;
  private static final float DISJOINT_COEFFICIENT = 1.0f;
  private static final float WEIGHT_DIFF_COEFFICIENT = 0.4f;
  private static final int DEFAULT_SPECIES = 0;

  public final int INPUT_COUNT;
  public final int OUTPUT_COUNT;

  public Genome(int inputCount, int outputCount, HashMap<ConnectionGene, Integer> innovations) {
    this.INPUT_COUNT = inputCount;
    this.OUTPUT_COUNT = outputCount;
    this.nodeGenes = new ArrayList<>();
    this.connections = new ArrayList<>();
    this.rng = new Random();

    setSpecies(DEFAULT_SPECIES);
    initialiseGenome(innovations);
  }

  public static float compatibilityDistance(Genome genome1, Genome genome2, HashMap<ConnectionGene, Integer> innovations) {
    int matchingConnections = 0;
    int totalWeightDiff = 0;

    for (Connection connection1 : genome1.connections) {
      for (Connection connection2 : genome2.connections) {
        int innovationNumber1 = innovations.get(connection1.getGene());
        int innovationNumber2 = innovations.get(connection2.getGene());

        if (innovationNumber1 == innovationNumber2) {
          matchingConnections++;

          totalWeightDiff += Math.abs(connection1.getWeight() - connection2.getWeight());
        }
      }
    }

    int excessConnections = excessConnections(genome1, genome2, innovations).size();
    int disjointConnections1 = genome1.connections.size() - excessConnections - matchingConnections;
    int disjointConnections2 = genome2.connections.size() - excessConnections - matchingConnections;
    int disjointConnections = disjointConnections1 + disjointConnections2;
    float avgWeightDiff = (float) totalWeightDiff / (float) matchingConnections;

    float weightedExcess = EXCESS_COEFFICIENT * excessConnections;
    float weightedDisjoint = DISJOINT_COEFFICIENT * disjointConnections;
    float weightedAvgWeightDiff = WEIGHT_DIFF_COEFFICIENT * avgWeightDiff;

    float maxGeneCount = Math.max(genome1.connections.size(), genome2.connections.size());
    maxGeneCount = maxGeneCount < 20 ? 1 : maxGeneCount;

    return (weightedExcess + weightedDisjoint) / maxGeneCount + weightedAvgWeightDiff;
  }

  public static ArrayList<Connection> excessConnections(Genome genome1, Genome genome2, HashMap<ConnectionGene, Integer> innovations) {
    int genome1Max = genome1.maxInnovationNumber(innovations);
    int genome2Max = genome2.maxInnovationNumber(innovations);

    ArrayList<Connection> excessConnections = new ArrayList<>();

    Genome larger;
    int smallerMax;

    if (genome1Max > genome2Max) {
      larger = genome1;
      smallerMax = genome2Max;
    } else {
      larger = genome2;
      smallerMax = genome1Max;
    }

    for (Connection connection : larger.connections) {
      if (innovations.get(connection.getGene()) > smallerMax) {
        excessConnections.add(connection);
      }
    }

    return excessConnections;
  }

  private int maxInnovationNumber(HashMap<ConnectionGene, Integer> innovations) {
    int max = Integer.MIN_VALUE;

    for (Connection connection : connections) {
      int innovationNumber = innovations.get(connection.getGene());

      if (innovationNumber > max) {
        max = innovationNumber;
      }
    }

    return max;
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

  public int getSpecies() {
    return species;
  }

  public void setSpecies(int species) {
    this.species = species;
  }
}
