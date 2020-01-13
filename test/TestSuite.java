import org.junit.Test;

import java.util.HashMap;
import java.util.Random;
import static org.junit.Assert.*;

public class TestSuite {

  @Test
  public void genomeInitialises() {
    Random rng = new Random();
    HashMap<ConnectionGene, Integer> innovations = new HashMap<>();

    int inputCount = rng.nextInt(20) + 1;
    int outputCount = rng.nextInt(20) + 1;

    Genome genome = new Genome(inputCount, outputCount, innovations);

    int connectionGeneLength = inputCount * outputCount;

    for (int i = 0; i < connectionGeneLength; i++) {
      Connection connection = genome.getConnection(i);
      NodeType in = genome.getNodeGene(connection.getIn());
      NodeType out = genome.getNodeGene(connection.getOut());

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
    HashMap<ConnectionGene, Integer> innovations = new HashMap<>();

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
}