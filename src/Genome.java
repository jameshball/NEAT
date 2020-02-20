import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Genome {
  private State state;
  private List<NodeType> nodes;
  private List<Connection> connections;
  private Random rng;
  private int species;
  private float fitness;
  private Population population;

  private static final int DEFAULT_SPECIES = 0;
  private static final float EXCESS_COEFFICIENT = 1.0f;
  private static final float DISJOINT_COEFFICIENT = 1.0f;
  private static final float WEIGHT_DIFF_COEFFICIENT = 0.4f;
  private static final float WEIGHT_MUTATION_RATE = 0.8f;
  private static final float UNIFORM_PERTURBATION_RATE = 1f;
  private static final float ADD_NEW_NODE_RATE = 0.03f;
  private static final float ADD_NEW_CONNECTION_RATE = 0.05f;
  private static final float SIGMOID_CONSTANT = 4.9f;
  // Decreasing this may dramatically affect performance.
  private static final float ACTIVATION_STABILISATION_THRESHOLD = 0.02f;

  public final int INPUT_COUNT;
  public final int OUTPUT_COUNT;

  public Genome(int inputCount, int outputCount, State state, Random rng, Population population) {
    this.INPUT_COUNT = inputCount;
    this.OUTPUT_COUNT = outputCount;
    this.state = state.reset().deepCopy();
    this.nodes = new ArrayList<>();
    this.connections = new ArrayList<>();
    this.rng = rng;
    this.fitness = 0;
    this.population = population;

    setSpecies(DEFAULT_SPECIES);
    initialiseGenome();
  }

  public Genome(int inputCount, int outputCount, List<Connection> connections, Genome parent, Population population) {
    this.INPUT_COUNT = inputCount;
    this.OUTPUT_COUNT = outputCount;
    this.state = parent.getState().reset().deepCopy();
    this.connections = connections;
    this.nodes = new ArrayList<>();
    this.rng = new Random();
    this.fitness = 0;
    this.population = population;

    nodes.addAll(parent.getNodes());
    setSpecies(DEFAULT_SPECIES);
  }

  public Genome(int inputCount, int outputCount, State state, Population population) {
    this(inputCount, outputCount, state, new Random(), population);
  }

  public void updateState() {
    state.update(activate(state.getGenomeInputs()));
  }

  private float[] activate(float[] inputs) {
    assert inputs.length == INPUT_COUNT;

    float[] nodeValues = new float[nodeCount()];
    float[] prevNodeValues = new float[nodeCount()];

    for (int i = 0; i < inputs.length; i++) {
      nodeValues[i] = inputs[i];
      prevNodeValues[i] = inputs[i];
    }

    do {
      for (int i = INPUT_COUNT; i < nodeCount(); i++) {
        prevNodeValues[i] = nodeValues[i];
      }

      for (int i = INPUT_COUNT; i < nodeCount(); i++) {
        nodeValues[i] = 0;

        for (Connection connection : connections) {
          if (!connection.isDisabled() && connection.getOut() == i) {
            nodeValues[i] += prevNodeValues[connection.getIn()] * connection.getWeight();
          }
        }

        nodeValues[i] = sigmoid(nodeValues[i]);
      }
    } while (!isStabilised(nodeValues, prevNodeValues));

    float[] outputs = new float[OUTPUT_COUNT];

    for (int i = INPUT_COUNT; i < INPUT_COUNT + OUTPUT_COUNT; i++) {
      outputs[i - INPUT_COUNT] = nodeValues[i];
    }

    return outputs;
  }

  // TODO: Implement activation function in State, so it can be user defined.
  private static float sigmoid(float x) {
    return (float) (1 / (1 + Math.exp(-SIGMOID_CONSTANT * x)));
  }

  private boolean isStabilised(float[] nodeValues, float[] prevNodeValues) {
    assert nodeValues.length == prevNodeValues.length;

    float totalRelDiff = 0;

    for (int i = INPUT_COUNT; i < nodeValues.length; i++) {
      if (nodeValues[i] != prevNodeValues[i]) {
        totalRelDiff += Math.abs((nodeValues[i] - prevNodeValues[i]) / prevNodeValues[i]);
      }
    }

    return totalRelDiff < ACTIVATION_STABILISATION_THRESHOLD;
  }

  public float compatibilityDistance(Genome genome, Map<ConnectionGene, Integer> innovations) {
    int matchingConns = 0;
    int totalWeightDiff = 0;

    for (Connection conn1 : connections) {
      for (Connection conn2 : genome.connections) {
        int innovationNum1 = innovations.get(conn1.getGene());
        int innovationNum2 = innovations.get(conn2.getGene());

        if (innovationNum1 == innovationNum2) {
          matchingConns++;
          totalWeightDiff += Math.abs(conn1.getWeight() - conn2.getWeight());
        }
      }
    }

    int excessConns = numberOfExcessConnections(genome, innovations);
    int disjointConns1 = connections.size() - excessConns - matchingConns;
    int disjointConns2 = genome.connections.size() - excessConns - matchingConns;
    int disjointConns = disjointConns1 + disjointConns2;
    float avgWeightDiff = (float) totalWeightDiff / (float) matchingConns;

    float maxGeneCount = Math.max(connections.size(), genome.connections.size());
    maxGeneCount = maxGeneCount < 20 ? 1 : maxGeneCount;

    return (EXCESS_COEFFICIENT * excessConns + DISJOINT_COEFFICIENT * disjointConns)
           / (maxGeneCount + WEIGHT_DIFF_COEFFICIENT * avgWeightDiff);
  }

  public int numberOfExcessConnections(Genome genome, Map<ConnectionGene, Integer> innovations) {
    int genome1Max = maxInnovationNumber(innovations);
    int genome2Max = genome.maxInnovationNumber(innovations);

    int excessConns = 0;

    Genome larger = genome1Max > genome2Max ? this : genome;
    int smallerMax = Math.min(genome1Max, genome2Max);

    for (Connection connection : larger.connections) {
      if (innovations.get(connection.getGene()) > smallerMax) {
        excessConns++;
      }
    }

    return excessConns;
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

  public float evaluateFitness() {
    return state.evaluateFitness();
  }

  public void mutate() {
    mutateAddNode();
    mutateAddConnection();
    mutateWeights();
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

  public void mutateAddNode() {
    if (rng.nextFloat() < ADD_NEW_NODE_RATE) {
      int randomConnIndex = rng.nextInt(connections.size());

      Connection connection = connections.get(randomConnIndex);
      connection.disable();

      int newNodeId = addNode(NodeType.HIDDEN);
      ConnectionGene gene1 = new ConnectionGene(connection.getIn(), newNodeId);
      ConnectionGene gene2 = new ConnectionGene(newNodeId, connection.getOut());

      addConnection(gene1, 1);
      addConnection(gene2, connection.getWeight());
    }
  }

  public void mutateAddConnection() {
    if (rng.nextFloat() < ADD_NEW_CONNECTION_RATE) {
      List<ConnectionGene> missingGenes = missingGenes();

      if (missingGenes.isEmpty()) {
        return;
      }

      int randomIndex = rng.nextInt(missingGenes.size());

      addConnection(missingGenes.get(randomIndex), randomWeight());
    }
  }

  private List<ConnectionGene> missingGenes() {
    List<ConnectionGene> genes = new ArrayList<>();

    for (int i = 0; i < nodes.size(); i++) {
      for (int j = 0; j < nodes.size(); j++) {
        if (i != j) {
          ConnectionGene gene = new ConnectionGene(i, j);

          if (getNode(i) != NodeType.OUTPUT && getNode(j) != NodeType.INPUT) {
            if (!containsConnectionGene(gene)) {
              genes.add(gene);
            }
          }
        }
      }
    }

    return genes;
  }

  private boolean containsConnectionGene(ConnectionGene gene) {
    for (Connection connection : connections) {
      if (connection.getGene().equals(gene)) {
        return true;
      }
    }

    return false;
  }

  private float randomWeight() {
    return rng.nextFloat() * 2 - 1;
  }

  private void initialiseGenome() {
    for (int i = 0; i < INPUT_COUNT; i++) {
      addNode(NodeType.INPUT);
    }

    for (int i = INPUT_COUNT; i < initialNodeCount(); i++) {
      addNode(NodeType.OUTPUT);

      for (int j = 0; j < INPUT_COUNT; j++) {
        ConnectionGene gene = new ConnectionGene(j, i);

        addConnection(gene, randomWeight());
      }
    }
  }

  private void addConnection(ConnectionGene gene, float weight) {
    population.addInnovation(gene);
    connections.add(new Connection(gene, weight));
  }

  private int addNode(NodeType node) {
    nodes.add(node);
    return nodes.size() - 1;
  }

  public NodeType getNode(int index) {
    return nodes.get(index);
  }

  public List<NodeType> getNodes() {
    return nodes;
  }

  public int nodeCount() {
    return nodes.size();
  }

  public Connection getConnection(int index) {
    return connections.get(index);
  }

  public Connection getConnection(ConnectionGene gene) {
    for (Connection connection : connections) {
      if (connection.getGene().equals(gene)) {
        return connection;
      }
    }

    return null;
  }

  public List<Connection> getConnections() {
    List<Connection> connsCopy = new ArrayList<>();

    for (Connection connection : connections) {
      connsCopy.add(connection.copy());
    }

    return connsCopy;
  }

  public int connectionCount() {
    return connections.size();
  }

  public boolean containsNode(NodeType node, int index) {
    return getNode(index).equals(node);
  }

  private int initialNodeCount() {
    return INPUT_COUNT + OUTPUT_COUNT;
  }

  public int getSpecies() {
    return species;
  }

  public void setSpecies(int species) {
    this.species = species;
  }

  private State getState() {
    return state;
  }

  public boolean hasEnded() {
    return state.hasEnded();
  }

  public float getFitness() {
    return fitness;
  }

  public void setFitness(float fitness) {
    this.fitness = fitness;
  }
}