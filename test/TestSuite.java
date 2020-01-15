import org.junit.Test;
import org.w3c.dom.Node;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;

public class TestSuite {

  // TODO: Write tests for genome mutation.

  @Test
  public void genomeInitialises() {
    Random rng = new Random();
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
      assertEquals(in, NodeType.INPUT);
      assertEquals(out, NodeType.OUTPUT);
      assertTrue(genome.containsNode(in, connection.getIn()));
      assertTrue(genome.containsNode(out, connection.getOut()));
    }
  }

  @Test
  public void testInnovations() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Arbitrary input and output counts.
    int inputCount = 10;
    int outputCount = 10;
    Genome genome = new Genome(inputCount, outputCount, innovations);

    assertEquals(innovations.size(), inputCount * outputCount);

    ConnectionGene dummyGene = new ConnectionGene(Integer.MAX_VALUE, 0);

    Population.addInnovation(dummyGene, innovations);
    assertEquals(innovations.size(), inputCount * outputCount + 1);
    Population.addInnovation(dummyGene, innovations);
    assertEquals(innovations.size(), inputCount * outputCount + 1);
  }

  @Test
  public void testAddNodeMutation() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Arbitrary input and output counts.
    int inputCount = 10;
    int outputCount = 10;

    // Seed of 5 results in a node being 'randomly' added.
    Genome genome = new Genome(inputCount, outputCount, new Random(5), innovations);
    genome.mutateAddNode(innovations);

    assertEquals(genome.nodeCount(), inputCount + outputCount + 1);
    assertEquals(genome.connectionCount(), inputCount * outputCount + 2);

    int in = genome.getConnection(genome.connectionCount() - 2).getIn();
    int out = genome.getConnection(genome.connectionCount() - 1).getOut();
    ConnectionGene gene = new ConnectionGene(in, out);

    assertTrue(genome.getConnection(gene).isDisabled());

    int newNode = genome.getConnection(genome.connectionCount() - 1).getIn();

    assertEquals(newNode, genome.nodeCount() - 1);
    assertEquals(genome.getNode(genome.nodeCount() - 1), NodeType.HIDDEN);
  }

  @Test
  public void testAddConnectionMutation() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Arbitrary input and output counts.
    int inputCount = 10;
    int outputCount = 10;

    // Seed of 5 results in a connection being 'randomly' added.
    Genome genome = new Genome(inputCount, outputCount, new Random(5), innovations);
    genome.mutateAddConnection(innovations);

    // There are no free connections initially, so none should be added.
    assertEquals(genome.connectionCount(), inputCount * outputCount);
    assertEquals(innovations.size(), inputCount * outputCount);

    innovations = new Hashtable<>();
    // Seed of 503 results in a connection and node being 'randomly' added.
    genome = new Genome(inputCount, outputCount, new Random (503), innovations);
    genome.mutateAddNode(innovations);
    genome.mutateAddConnection(innovations);

    assertEquals(innovations.size(), inputCount * outputCount + 3);

    int in = genome.getConnection(genome.connectionCount() - 1).getIn();
    int out = genome.getConnection(genome.connectionCount() - 1).getOut();

    assertNotEquals(genome.getNode(in), NodeType.OUTPUT);
    assertNotEquals(genome.getNode(out), NodeType.INPUT);
    assertTrue(innovations.containsKey(new ConnectionGene(in, out)));
    assertTrue(genome.getNode(in).equals(NodeType.HIDDEN)
            || genome.getNode(out).equals(NodeType.HIDDEN));
  }

  @Test
  public void testWeightMutation() {
    Map<ConnectionGene, Integer> innovations = new Hashtable<>();

    // Arbitrary input and output counts.
    int inputCount = 10;
    int outputCount = 10;

    // Seed of 5 results in weights being mutated.
    Genome genome = new Genome(inputCount, outputCount, new Random(5), innovations);

    float[] weights = new float[genome.connectionCount()];

    for (int i = 0; i < genome.connectionCount(); i++) {
      weights[i] = genome.getConnection(i).getWeight();
    }

    genome.mutateWeights();

    for (int i = 0; i < genome.connectionCount(); i++) {
      float newWeight = genome.getConnection(i).getWeight();

      assertNotEquals(newWeight, weights[i]);
      assertTrue(newWeight <= 1 && newWeight >= -1);
    }
  }
}