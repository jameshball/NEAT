import org.junit.Test;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;

public class TestSuite {
  private static final Random rng = new Random();
  private static final BlankState blankState = new BlankState();

  // Arbitrary input and output counts for testing.
  private static final int INPUT_COUNT = 10;
  private static final int OUTPUT_COUNT = 10;

  // TODO: Test crossover, placeInSpecies, and activate functions.

  @Test
  public void genomeInitialises() {
    int inputCount = rng.nextInt(20) + 1;
    int outputCount = rng.nextInt(20) + 1;

    Population population = new Population(0, inputCount, outputCount, new BlankState());

    Genome genome = new Genome(inputCount, outputCount, blankState, population);

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
    int inputCount = rng.nextInt(20) + 1;
    int outputCount = rng.nextInt(20) + 1;

    Population population = new Population(0, inputCount, outputCount, new BlankState());

    Genome genome = new Genome(inputCount, outputCount, blankState, population);

    assertEquals(inputCount * outputCount, population.innovationsSize());

    ConnectionGene dummyGene = new ConnectionGene(Integer.MAX_VALUE, 0);

    population.addInnovation(dummyGene);
    assertEquals(inputCount * outputCount + 1, population.innovationsSize());
    population.addInnovation(dummyGene);
    assertEquals(inputCount * outputCount + 1, population.innovationsSize());
  }

  @Test
  public void testAddNodeMutation() {
    Population population = new Population(0, INPUT_COUNT, OUTPUT_COUNT, new BlankState());

    // Seed of 5 results in a node being 'randomly' added.
    Genome genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, blankState, new Random(5), population);
    genome.mutateAddNode();

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
    Population population = new Population(0, INPUT_COUNT, OUTPUT_COUNT, new BlankState());

    // Seed of 5 results in a connection being 'randomly' added.
    Genome genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, blankState, new Random(5), population);
    genome.mutateAddConnection();

    // There are no free connections initially, so none should be added.
    assertEquals(INPUT_COUNT * OUTPUT_COUNT, genome.connectionCount());
    assertEquals(INPUT_COUNT * OUTPUT_COUNT, population.innovationsSize());

    population = new Population(0, INPUT_COUNT, OUTPUT_COUNT, new BlankState());
    // Seed of 503 results in a connection and node being 'randomly' added.
    genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, blankState, new Random(503), population);
    genome.mutateAddNode();
    genome.mutateAddConnection();

    assertEquals(INPUT_COUNT * OUTPUT_COUNT + 3, population.innovationsSize());

    int in = genome.getConnection(genome.connectionCount() - 1).getIn();
    int out = genome.getConnection(genome.connectionCount() - 1).getOut();

    assertNotEquals(NodeType.OUTPUT, genome.getNode(in));
    assertNotEquals(NodeType.INPUT, genome.getNode(out));
    assertTrue(
        genome.getNode(in).equals(NodeType.HIDDEN) || genome.getNode(out).equals(NodeType.HIDDEN));
  }

  @Test
  public void testWeightMutation() {
    Population population = new Population(0, INPUT_COUNT, OUTPUT_COUNT, new BlankState());

    // Seed of 5 results in weights being mutated.
    Genome genome = new Genome(INPUT_COUNT, OUTPUT_COUNT, blankState, new Random(5), population);

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
