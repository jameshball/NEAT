import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
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
      NodeGene in = genome.getNodeGene(connection.getIn());
      NodeGene out = genome.getNodeGene(connection.getOut());

      assertNotNull(connection);
      assertNotNull(in);
      assertNotNull(out);
      assertEquals(in.getType(), NodeType.INPUT);
      assertEquals(out.getType(), NodeType.OUTPUT);
      assertTrue(genome.containsNode(in));
      assertTrue(genome.containsNode(out));
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