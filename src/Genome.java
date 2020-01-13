import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Genome {
  private List<NodeType> nodes;
  private List<Connection> connections;
  private Random rng;
  private int species;

  private static final float EXCESS_COEFFICIENT = 1.0f;
  private static final float DISJOINT_COEFFICIENT = 1.0f;
  private static final float WEIGHT_DIFF_COEFFICIENT = 0.4f;
  private static final int DEFAULT_SPECIES = 0;
  private static final float WEIGHT_MUTATION_RATE = 0.8f;
  private static final float UNIFORM_PERTURBATION_RATE = 0.9f;
  private static final float ADD_NEW_NODE_RATE = 0.03f;
  private static final float ADD_NEW_CONNECTION_RATE = 0.05f;

  public final int INPUT_COUNT;
  public final int OUTPUT_COUNT;

  public Genome(int inputCount, int outputCount, Map<ConnectionGene, Integer> innovations) {
    this.INPUT_COUNT = inputCount;
    this.OUTPUT_COUNT = outputCount;
    this.nodes = new ArrayList<>();
    this.connections = new ArrayList<>();
    this.rng = new Random();

    setSpecies(DEFAULT_SPECIES);
    initialiseGenome(innovations);
  }

  public static float compatibilityDistance(Genome genome1, Genome genome2, Map<ConnectionGene, Integer> innovations) {
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

  public static ArrayList<Connection> excessConnections(Genome genome1, Genome genome2, Map<ConnectionGene, Integer> innovations) {
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

  private int maxInnovationNumber(Map<ConnectionGene, Integer> innovations) {
    int max = Integer.MIN_VALUE;

    for (Connection connection : connections) {
      int innovationNumber = innovations.get(connection.getGene());

      if (innovationNumber > max) {
        max = innovationNumber;
      }
    }

    return max;
  }

  public void mutateWeights() {
    if (rng.nextFloat() < WEIGHT_MUTATION_RATE) {
      for (Connection connection : connections) {
        if (rng.nextFloat() < UNIFORM_PERTURBATION_RATE) {
          float weight = connection.getWeight();

          connection.setWeight((float) (weight + rng.nextGaussian() / 5));
        } else {
          connection.setWeight(randomWeight());
        }
      }
    }
  }

  public void mutateAddNode(Map<ConnectionGene, Integer> innovations) {
    if (rng.nextFloat() < ADD_NEW_NODE_RATE) {
      int randomConnectionIndex = rng.nextInt(connections.size());

      Connection connection = connections.get(randomConnectionIndex);
      connection.disable();

      int newNodeId = addNode(NodeType.HIDDEN);
      ConnectionGene gene1 = new ConnectionGene(connection.getIn(), newNodeId);
      ConnectionGene gene2 = new ConnectionGene(newNodeId, connection.getOut());

      addConnection(gene1, 1, innovations);
      addConnection(gene2, connection.getWeight(), innovations);
    }
  }

  private float randomWeight() {
    return rng.nextFloat() * 2 - 1;
  }

  private void initialiseGenome(Map<ConnectionGene, Integer> innovations) {
    for (int i = 0; i < INPUT_COUNT; i++) {
      addNode(NodeType.INPUT);
    }

    for (int i = INPUT_COUNT; i < nodeCount(); i++) {
      addNode(NodeType.OUTPUT);

      for (int j = 0; j < INPUT_COUNT; j++) {
        ConnectionGene gene = new ConnectionGene(j, i);

        addConnection(gene, randomWeight(), innovations);
      }
    }
  }

  private void addConnection(ConnectionGene gene, float weight, Map<ConnectionGene, Integer> innovations) {
    Population.addInnovation(gene, innovations);
    connections.add(new Connection(gene, weight));
  }

  private int addNode(NodeType node) {
    nodes.add(node);

    return nodes.size() - 1;
  }

  public NodeType getNodeGene(int index) {
    return nodes.get(index);
  }

  public Connection getConnection(int index) {
    return connections.get(index);
  }

  public boolean containsNode(NodeType node, int index) {
    return getNodeGene(index).equals(node);
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
