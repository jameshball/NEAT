import org.junit.Test;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;

public class TestSuite {
  private static Random rng = new Random();

  // Arbitrary input and output counts for testing.
  private static final int INPUT_COUNT = 10;
  private static final int OUTPUT_COUNT = 10;

  // TODO: Create a more convincing testPartitionConnections test.

  @Test
  public void testPartitionConnections() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    Genome genome1 = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random(0), innovations);
    Genome genome2 = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random(0), innovations);
    Genome genome3 = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random(5), innovations);
    genome3.mutateAddNode(innovations);

    List<Connection>[] partitionedConnections1 = genome1.partitionConnections(genome2, innovations);
    List<Connection>[] partitionedConnections2 = genome1.partitionConnections(genome3, innovations);

    List<Connection> matchingConnections1 = partitionedConnections1[0];
    List<Connection> matchingConnections2 = partitionedConnections2[0];

    assertEquals(matchingConnections1.size(), matchingConnections2.size());

    List<Connection> disjointConnections1 = partitionedConnections1[1];
    List<Connection> disjointConnections2 = partitionedConnections2[1];

    assertTrue(disjointConnections1.isEmpty());
    assertTrue(disjointConnections2.isEmpty());

    List<Connection> excessConnections1 = partitionedConnections1[2];
    List<Connection> excessConnections2 = partitionedConnections2[2];

    assertTrue(excessConnections1.isEmpty());
    assertEquals(2, excessConnections2.size());
    assertEquals(excessConnections2.get(1).getIn(), excessConnections2.get(0).getOut());

    int totalConnections = matchingConnections2.size() + disjointConnections2.size() + excessConnections2.size();
    assertEquals(genome3.connectionCount(), totalConnections);
  }

  @Test
  public void genomeInitialises() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    int inputCount = rng.nextInt(20) + 1;
    int outputCount = rng.nextInt(20) + 1;

    Genome genome = new Genome(inputCount, outputCount, innovations);

    int connectionGeneLength = inputCount * outputCount;

    for (int i = 0; i < connectionGeneLength; i++) {
      Connection connection = genome.getConnection(i);
      NodeType in = genome.getNode(connection.getIn());
      NodeType out = genome.getNode(connection.getOut());

      assertNotNull(connection);
      assertNotNull(in);
      assertNotNull(out);
      assertEquals(NodeType.INPUT, in);
      assertEquals(NodeType.OUTPUT, out);
      assertTrue(genome.containsNode(in, connection.getIn()));
      assertTrue(genome.containsNode(out, connection.getOut()));
    }
  }

  @Test
  public void testInnovations() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    int inputCount = rng.nextInt(20) + 1;
    int outputCount = rng.nextInt(20) + 1;
    // Genome isn't used, but updates innovations map.
    Genome genome = new Genome(inputCount, outputCount, innovations);

    assertEquals(innovations.size(), inputCount * outputCount);

    ConnectionGene dummyGene = new ConnectionGene(Integer.MAX_VALUE, 0);

    Population.addInnovation(dummyGene, innovations);
    assertEquals(inputCount * outputCount + 1, innovations.size());
    Population.addInnovation(dummyGene, innovations);
    assertEquals(inputCount * outputCount + 1, innovations.size());
  }

  @Test
  public void testAddNodeMutation() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Seed of 5 results in a node being 'randomly' added.
    Genome genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random(5), innovations);
    genome.mutateAddNode(innovations);

    assertEquals(INPUT_COUNT + OUTPUT_COUNT + 1, genome.nodeCount());
    assertEquals(INPUT_COUNT * OUTPUT_COUNT + 2, genome.connectionCount());

    int in = genome.getConnection(genome.connectionCount() - 2).getIn();
    int out = genome.getConnection(genome.connectionCount() - 1).getOut();
    ConnectionGene gene = new ConnectionGene(in, out);

    assertTrue(genome.getConnection(gene).isDisabled());

    int newNode = genome.getConnection(genome.connectionCount() - 1).getIn();

    assertEquals(genome.nodeCount() - 1, newNode);
    assertEquals(NodeType.HIDDEN, genome.getNode(genome.nodeCount() - 1));
  }

  @Test
  public void testAddConnectionMutation() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Seed of 5 results in a connection being 'randomly' added.
    Genome genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random(5), innovations);
    genome.mutateAddConnection(innovations);

    // There are no free connections initially, so none should be added.
    assertEquals(INPUT_COUNT * OUTPUT_COUNT, genome.connectionCount());
    assertEquals(INPUT_COUNT * OUTPUT_COUNT, innovations.size());

    innovations = new Hashtable<>();
    // Seed of 503 results in a connection and node being 'randomly' added.
    genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random (503), innovations);
    genome.mutateAddNode(innovations);
    genome.mutateAddConnection(innovations);

    assertEquals(INPUT_COUNT * OUTPUT_COUNT + 3, innovations.size());

    int in = genome.getConnection(genome.connectionCount() - 1).getIn();
    int out = genome.getConnection(genome.connectionCount() - 1).getOut();

    assertNotEquals(NodeType.OUTPUT, genome.getNode(in));
    assertNotEquals(NodeType.INPUT, genome.getNode(out));
    assertTrue(innovations.containsKey(new ConnectionGene(in, out)));
    assertTrue(genome.getNode(in).equals(NodeType.HIDDEN)
            || genome.getNode(out).equals(NodeType.HIDDEN));
  }

  @Test
  public void testWeightMutation() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Seed of 5 results in weights being mutated.
    Genome genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, new Random(5), innovations);

    float[] weights = new float[genome.connectionCount()];

    for (int i = 0; i < genome.connectionCount(); i++) {
      weights[i] = genome.getConnection(i).getWeight();
    }

    genome.mutateWeights();

    for (int i = 0; i < genome.connectionCount(); i++) {
      float newWeight = genome.getConnection(i).getWeight();

      assertNotEquals(weights[i], newWeight);
      assertTrue(newWeight <= 1 && newWeight >= -1);
    }
  }
}